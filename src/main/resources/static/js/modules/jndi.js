window.JndiModule = {
    taskId: null,
    logTimer: null,

    init: async function() {
        if (!document.getElementById('jndiIp')) return;

        this.log("[*] Initializing JNDI Module...");

        try {
            const [meta, gadgetTypes, activeTask] = await Promise.all([
                window.JndiAPI.getMeta(),
                window.GadgetAPI.getTypes(),
                window.http.get('/jndi/active-task')
            ]);

            if (!meta || !meta.jndiTypes) {
                this.log("[!] Error: API Meta is null. Check backend connection.");
                return;
            }

            this.renderSelect('jndiType', meta.jndiTypes);
            this.renderSelect('jndiBypass', meta.bypassTypes);
            this.renderSelect('jndiGadget', gadgetTypes, true);
            const savedIp = localStorage.getItem('jndi_ip');
            document.getElementById('jndiIp').value = savedIp || meta.localIp;

            if (activeTask && activeTask.active) {
                this.taskId = activeTask.taskId;
                const req = activeTask.request;

                document.getElementById('jndiType').value = req.type;
                if(req.bypassType) document.getElementById('jndiBypass').value = req.bypassType;
                if(req.gadget) document.getElementById('jndiGadget').value = req.gadget;
                document.getElementById('jndiCommand').value = req.command;

                this.onTypeChange();
                this.updateUI(true);
                this.updateLinks(req.type, req.ip, activeTask.rmiPort, activeTask.ldapPort);
                this.startPolling();
                this.log(`[+] Session restored (ID: ${this.taskId})`);
            } else {
                this.onTypeChange();
                this.log("[*] System Ready. Waiting for commands...");
            }
        } catch (e) {
            console.error(e);
            this.log("[!] Init Exception: " + e.message);
        }
    },

    renderSelect: function(id, list, addEmpty = false) {
        const $s = document.getElementById(id);
        if(!$s) return;
        $s.innerHTML = addEmpty ? '<option value="">-- Select --</option>' : '';
        if (list && Array.isArray(list)) {
            list.forEach(item => $s.add(new Option(item, item)));
        }
    },

    onTypeChange: function() {
        const type = document.getElementById('jndiType').value;
        const $bypass = document.getElementById('jndiBypass');
        const $gadget = document.getElementById('jndiGadget');
        if ($bypass) $bypass.disabled = (type !== 'RMI_LOCAL_FACTORY');
        if ($gadget) $gadget.disabled = (type !== 'LDAP_SERIALIZED');
    },

    updateLinks: function(type, ip, rmi, ldap) {
        const l = document.getElementById('resLdap');
        const r = document.getElementById('resRmi');
        if(l) l.value = ldap > 0 ? `ldap://${ip}:${ldap}/Object` : "[Skipped]";
        if(r) r.value = rmi > 0 ? `rmi://${ip}:${rmi}/Object` : "[Skipped]";
    },

    start: async function() {
        const payload = {
            ip: document.getElementById('jndiIp').value,
            type: document.getElementById('jndiType').value,
            bypassType: document.getElementById('jndiBypass').value,
            gadget: document.getElementById('jndiGadget').value,
            command: document.getElementById('jndiCommand').value
        };
        localStorage.setItem('jndi_ip', payload.ip);
        this.log(`[*] Starting server on ${payload.ip}...`);

        try {
            const res = await window.JndiAPI.start(payload);
            if (res && (res.success || res.active)) {
                this.taskId = res.taskId;
                this.updateUI(true);
                this.updateLinks(payload.type, payload.ip, res.rmiPort, res.ldapPort);
                this.startPolling();
                this.log(`[+] Server started successfully. (Ports: RMI=${res.rmiPort}, LDAP=${res.ldapPort})`);
            } else {
                this.log(`[!] Start failed: ${res.msg || 'Unknown error'}`);
            }
        } catch(e) {
            this.log(`[!] Connection Error: ${e.message}`);
        }
    },

    stop: async function() {
        let idToStop = this.taskId;

        if (!idToStop) {
            this.log("[*] Checking backend for orphan tasks...");
            try {
                const activeTask = await window.http.get('/jndi/active-task');
                if (activeTask && activeTask.active) {
                    idToStop = activeTask.taskId;
                    this.taskId = idToStop;
                }
            } catch (e) {  }
        }

        if (!idToStop) {
            this.log("[!] No active task found to stop.");
            this.forceResetUI();
            return;
        }

        try {
            this.log(`[*] Stopping task ${idToStop}...`);
            await window.JndiAPI.stop(idToStop);
            this.log("[+] Server stopped.");
        } catch (e) {
            this.log(`[!] Stop failed: ${e.message}`);
        } finally {
            this.forceResetUI();
        }
    },

    forceResetUI: function() {
        this.stopPolling();
        this.updateUI(false);
        this.updateLinks('', '', 0, 0);
        this.taskId = null;
    },

    startPolling: function() {
        this.stopPolling();
        this.logTimer = setInterval(async () => {
            if(!document.getElementById('jndiLogArea')) { this.stopPolling(); return; }
            if(!this.taskId) return;

            try {
                const res = await window.JndiAPI.getStatus(this.taskId);
                if (res.logs) this.log(res.logs, false);
                if (res.running === false) {
                    this.log("[*] Task finished/terminated by server.");
                    this.stop();
                }
            } catch(e) { this.stopPolling(); }
        }, 1500);
    },

    stopPolling: function() {
        if(this.logTimer) {
            clearInterval(this.logTimer);
            this.logTimer = null;
        }
    },

    updateUI: function(running) {
        const btnStart = document.getElementById('btnStartJndi');
        const btnStop = document.getElementById('btnStopJndi');
        if(btnStart) btnStart.disabled = running;
        if(btnStop) btnStop.disabled = !running;

        const dot = document.getElementById('jndiStatusDot');
        const txt = document.getElementById('jndiStatusText');
        if(dot) dot.className = running ? "status status-dot status-green" : "status status-dot status-red";
        if(txt) txt.innerText = running ? "Running" : "Stopped";
    },

    log: function(msg, withTime = true) {
        const area = document.getElementById('jndiLogArea');
        if(!area) return;

        // 自动换行处理
        const text = (withTime ? `[${new Date().toLocaleTimeString()}] ` : "") + msg + "\n";
        area.value += text;
        area.scrollTop = area.scrollHeight;
    },

    clearLog: function() {
        const area = document.getElementById('jndiLogArea');
        if(area) area.value = '> Logs cleared.\n';
    }
};