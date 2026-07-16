window.ShiroModule = {
    taskId: null,
    timer: null,
    exploitContext: null,

    /**
     * 初始化模块
     */
    init: function() {
        if (!document.getElementById('targetUrl')) return;
        this.loadOptions();
        this.setStatus('idle');
    },

    /**
     * 从后端加载 Gadget 和 Echo 下拉框数据
     */
    loadOptions: async function() {
        try {
            const [gadgets, echos] = await Promise.all([
                window.ShiroAPI.getGadgets(),
                window.ShiroAPI.getEchos()
            ]);

            const gSelect = document.getElementById('gadgetSelect');
            const eSelect = document.getElementById('echoSelect');

            if (gSelect && gadgets) {
                // 保存当前选中的值（防止刷新重置）
                const currentVal = gSelect.value;
                gSelect.innerHTML = '<option value="">-- Gadget --</option>';
                gadgets.forEach(g => gSelect.add(new Option(g, g)));
                if(currentVal) gSelect.value = currentVal;
            }
            if (eSelect && echos) {
                const currentVal = eSelect.value;
                eSelect.innerHTML = '<option value="">-- Echo --</option>';
                echos.forEach(e => eSelect.add(new Option(e, e)));
                if(currentVal) eSelect.value = currentVal;
            }
        } catch (e) {
            this.log("error", "Failed to load options: " + e.message);
        }
    },

    /**
     * 统一日志输出
     */
    log: function(type, msg) {
        const area = document.getElementById('shiroLogArea');
        if (!area) return;

        const time = new Date().toLocaleTimeString();
        let colorClass = "text-body";
        let prefix = "[*]";

        if (type === 'error') { colorClass = "text-danger"; prefix = "[-]"; }
        if (type === 'success') { colorClass = "text-success"; prefix = "[+]"; }
        if (type === 'system') { colorClass = "text-primary"; prefix = "[i]"; }

        if (type === 'raw') {
            const div = document.createElement('div');
            div.className = "text-body mb-1 small";
            div.style.whiteSpace = "pre-wrap";
            div.style.paddingLeft = "0.5rem";
            div.innerText = msg;
            area.appendChild(div);
        } else {
            const row = document.createElement('div');
            row.className = "mb-1 small";
            row.innerHTML = `<span class="text-muted me-2">[${time}]</span><span class="${colorClass} fw-bold me-2">${prefix}</span><span class="${colorClass}">${msg}</span>`;
            area.appendChild(row);
        }

        area.scrollTop = area.scrollHeight;
    },

    /**
     * 启动扫描任务
     * @param mode: key_check, key_brute, gadget_check, gadget_brute
     */
    startScan: async function(mode) {
        this.exploitContext = null;

        const urlInput = document.getElementById('targetUrl');
        if (!urlInput || !urlInput.value) {
            this.log("error", "Target URL is required.");
            if(urlInput) {
                urlInput.focus();
                urlInput.classList.add("is-invalid");
                setTimeout(() => urlInput.classList.remove("is-invalid"), 2000);
            }
            return;
        }

        const keyVal = document.getElementById('specifiedKey')?.value.trim();
        const gadgetVal = document.getElementById('gadgetSelect')?.value;
        const echoVal = document.getElementById('echoSelect')?.value;
        const isGcm = document.getElementById('checkGCM')?.checked || false;
        const reqMethod = document.getElementById('reqMethod')?.value || 'GET';
        const timeout = parseInt(document.getElementById('timeout')?.value) || 10;

        const req = {
            url: urlInput.value,
            method: reqMethod,
            timeout: timeout,
            isGcm: isGcm,
            checkGadget: true
        };

        if (mode === 'key_check') {
            if (!keyVal) { this.log("error", "Please input a Key to check."); return; }
            req.specifiedKey = keyVal;
            req.checkGadget = false;
        }
        else if (mode === 'key_brute') {
            req.specifiedKey = null;
            req.checkGadget = false;
        }
        else if (mode.startsWith('gadget')) {
            if (!keyVal) {
                this.log("error", "Key is missing. Please Run Key Scan first or input manually.");
                return;
            }
            req.specifiedKey = keyVal;

            if (mode === 'gadget_check') {
                if (!gadgetVal || !echoVal) {
                    this.log("error", "Please select Gadget & Echo first.");
                    return;
                }
                req.specifiedGadget = gadgetVal;
                req.specifiedEcho = echoVal;
            } else {
                req.specifiedGadget = null;
            }
        }

        this.setRunningState(true);
        this.log("system", `Launching Task: ${mode.toUpperCase()} (Mode: ${isGcm ? 'GCM' : 'CBC'})`);

        try {
            const res = await window.ShiroAPI.start(req);
            if (res.success) {
                this.taskId = res.taskId;
                this.startPolling();
            } else {
                this.log("error", "API Error: " + (res.msg || "Start failed"));
                this.setRunningState(false);
            }
        } catch (e) {
            this.log("error", "Network Error: " + e.message);
            this.setRunningState(false);
        }
    },

    /**
     * 轮询状态
     */
    startPolling: function() {
        if(this.timer) clearInterval(this.timer);
        this.timer = setInterval(() => this.poll(), 1500);
        this.setStatus('running');
    },

    poll: async function() {
        if (!this.taskId) return;
        if (!document.getElementById('shiroLogArea')) {
            this.stopPolling();
            return;
        }

        try {
            const res = await window.ShiroAPI.status(this.taskId);

            if (res.logs && res.logs.length) this.log("raw", res.logs);

            // 发现 Key -> 自动填入输入框
            if (res.key) {
                // 记录到 context 备查，但主要动作是更新 UI
                if (!this.exploitContext) this.exploitContext = {};
                this.exploitContext.foundKey = res.key;

                const kInput = document.getElementById('specifiedKey');
                if(kInput && kInput.value !== res.key) {
                    kInput.value = res.key; // 更新 UI
                    kInput.classList.add("is-valid");
                    this.log("success", "KEY FOUND & FILLED: " + res.key);
                }
            }

            // 发现 Gadget -> 自动选中下拉框
            if (res.foundGadget) {
                if (!this.exploitContext) this.exploitContext = {};
                this.exploitContext.foundGadget = res.foundGadget;
                this.exploitContext.foundEcho = res.foundEchoType;

                const gSel = document.getElementById('gadgetSelect');
                const eSel = document.getElementById('echoSelect');

                if(gSel && res.foundGadget) gSel.value = res.foundGadget;
                if(eSel && res.foundEchoType) eSel.value = res.foundEchoType;

                this.log("success", `CHAIN FOUND & SELECTED: ${res.foundGadget}`);
            }

            if (!res.running) {
                this.stopPolling();
                this.log("system", "Scan Finished.");
            }
        } catch (e) {
            console.error(e);
            this.stopPolling();
        }
    },

    stopPolling: function() {
        if(this.timer) { clearInterval(this.timer); this.timer = null; }
        if (document.getElementById('shiroStatusDot')) {
            this.setRunningState(false);
            this.setStatus('idle');
        }
    },

    /**
     * 命令执行
     */
    execCmd: async function() {
        const cmdInput = document.getElementById('cmdInput');
        const cmd = cmdInput?.value.trim();

        if(!cmd) {
            this.log("error", "Command cannot be empty.");
            if(cmdInput) cmdInput.focus();
            return;
        }

        const url = document.getElementById('targetUrl').value;
        const key = document.getElementById('specifiedKey')?.value.trim();
        const gadget = document.getElementById('gadgetSelect')?.value;
        const echo = document.getElementById('echoSelect')?.value;
        const isGcm = document.getElementById('checkGCM')?.checked || false;
        const reqMethod = document.getElementById('reqMethod')?.value || 'GET';

        if(!key) {
            this.log("error", "Execution failed: Key is missing.");
            return;
        }
        if(!gadget) {
            this.log("error", "Execution failed: Gadget is not selected.");
            return;
        }

        this.log("system", `Executing command: ${cmd}`);

        try {
            const res = await window.ShiroAPI.exec({
                url: url,
                method: reqMethod,
                specifiedKey: key,
                isGcm: isGcm,
                specifiedGadget: gadget,
                specifiedEcho: echo,
                command: cmd
            });

            if(res.success) {
                this.log("raw", "\n" + res.data);
            } else {
                this.log("error", res.msg || "Execution failed without message.");
            }
        } catch (e) {
            this.log("error", "Exception: " + e.message);
        }
    },

    stop: async function() {
        if(this.taskId) await window.ShiroAPI.stop(this.taskId);
        this.stopPolling();
        this.log("system", "Task stopped by user.");
    },

    /**
     * UI 状态管理
     */
    setRunningState: function(running) {
        const btnStop = document.getElementById('btnStop');
        if (btnStop) {
            btnStop.disabled = !running;
        }
        const urlInput = document.getElementById('targetUrl');
        if (urlInput) {
            urlInput.disabled = running;
        }
    },

    setStatus: function(status) {
        const dot = document.getElementById('shiroStatusDot');
        const text = document.getElementById('shiroStatusText');
        if (!dot || !text) return;

        if (status === 'running') {
            dot.className = "status status-dot status-green status-dot-animated";
            text.innerText = "Scanning...";
        } else {
            dot.className = "status status-dot status-secondary";
            text.innerText = "Idle";
        }
    },

    clearLog: function() {
        const area = document.getElementById('shiroLogArea');
        if(area) area.innerHTML = '<div class="text-secondary small">[*] Log cleared.</div>';
    }
};