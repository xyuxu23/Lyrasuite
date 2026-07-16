/**
 * SnakeYAML Exploitation Module
 */
window.SnakeYamlModule = {
    logTimer: null,

    init: async function() {
        if (!document.getElementById('snakeType')) return;
        console.log("[SnakeYamlModule] Initializing...");

        try {
            //获取上次输入的ip
            const savedIp = localStorage.getItem('snake_spi_ip');
            if (savedIp) document.getElementById('spiIp').value = savedIp;

            const typesRes = await window.SnakeYamlAPI.getTypes();
            this.renderSelect('snakeType', typesRes);
            //尝试获取状态，恢复之前的日志监控
            await this.refreshStatus();

            //如果服务正在运行，开启轮询
            const statusDot = document.getElementById('spiStatusDot');
            if (statusDot && statusDot.classList.contains('status-green')) {
                this.startPolling();
            }

            this.onTypeChange();
            const inputs = ['snakeJndiUrl', 'spiIp', 'snakeCommand'];
            inputs.forEach(id => {
                const el = document.getElementById(id);
                if (el && !el.dataset.hasEnterListener) {
                    el.addEventListener('keydown', (e) => {
                        if (e.key === 'Enter') {
                            e.preventDefault();
                            this.generate();
                        }
                    });
                    el.dataset.hasEnterListener = "true";
                }
            });
        } catch (e) {
            console.error("Init failed:", e);
        }
    },

    renderSelect: function(id, list) {
        const $s = document.getElementById(id);
        if(!$s) return;
        $s.innerHTML = '';
        if (list && Array.isArray(list)) {
            list.forEach(item => $s.add(new Option(item, item)));
        }
        if (list.length > 0) $s.value = list[0];
    },

    onTypeChange: function() {
        const type = document.getElementById('snakeType').value;
        const groupJndi = document.getElementById('groupJndiUrl');
        const groupCmd = document.getElementById('groupCommand');
        const cardSpi = document.getElementById('cardSpiStatus');
        const cmdLabel = document.querySelector('#groupCommand label');
        const groupSpiIp = document.getElementById('groupSpiIp');
        const isSpi = type === 'ScriptEngineManager (SPI)';
        const isHex = type === 'C3P0 (Hex)';

        if (isSpi) {
            groupJndi.classList.add('d-none');
            groupCmd.classList.remove('d-none');
            cardSpi.classList.remove('d-none');
            groupSpiIp.classList.remove('d-none');

            if(cmdLabel) cmdLabel.innerText = "System Command";
            document.getElementById('snakeCommand').placeholder = "whoami";
        }
        else if (isHex) {
            groupJndi.classList.add('d-none');
            groupCmd.classList.remove('d-none');
            cardSpi.classList.add('d-none');
            groupSpiIp.classList.add('d-none');
            if(cmdLabel) cmdLabel.innerText = "Serialized Hex String";
            document.getElementById('snakeCommand').placeholder = "aced0005...";
        }
        else {
            groupJndi.classList.remove('d-none');
            groupCmd.classList.add('d-none');
            cardSpi.classList.add('d-none');
            groupSpiIp.classList.add('d-none');
        }
    },

    generate: async function() {
        const type = document.getElementById('snakeType').value;
        const jndiUrl = document.getElementById('snakeJndiUrl').value;
        const command = document.getElementById('snakeCommand').value;
        const ip = document.getElementById('spiIp').value;
        const resultArea = document.getElementById('snakeResult');

        const isSpi = type === 'ScriptEngineManager (SPI)';
        const isHex = type === 'C3P0 (Hex)';

        //  校验 SPI 模式下是否填写了 IP
        if (isSpi && !ip) {
            this.appendLog("Error: IP (SPI) is required.\n");
            return;
        }

        // 2. 校验 Command/Hex
        if ((isSpi || isHex) && !command) {
            this.appendLog(isHex ? "Error: Serialized Hex String is required.\n" : "Error: System Command is required.\n");
            return;
        }

        // 3. 校验 JNDI
        if (!isSpi && !isHex && !jndiUrl) {
            this.appendLog("Error: JNDI URL is required for this payload type.\n");
            return;
        }
        if (isSpi) {
            localStorage.setItem('snake_spi_ip', ip);
        }

        try {
            resultArea.value = "Generating...";

            const res = await window.SnakeYamlAPI.generate({
                type: type,
                jndiUrl: jndiUrl,
                command: command,
                ip: ip
            });

            if (res.success) {
                resultArea.value = res.data;
                if (isSpi) {
                    if (!this.logTimer) this.clearLog();
                    this.startPolling();
                    await this.refreshStatus();
                }
            } else {
                resultArea.value = "";
                this.appendLog("[!] Error: " + res.msg + "\n");
            }
        } catch (e) {
            resultArea.value = "";
            this.appendLog("[!] System Error: " + e.message + "\n");
        }
    },
    // --- 轮询与日志核心逻辑 ---
    startPolling: function() {
        this.stopPolling(); // 防止重复开启
        this.logTimer = setInterval(async () => {
            await this.refreshStatus();
        }, 1500); // 1.5秒刷新一次
    },

    stopPolling: function() {
        if(this.logTimer) {
            clearInterval(this.logTimer);
            this.logTimer = null;
        }
    },

    refreshStatus: async function() {
        try {
            const res = await window.SnakeYamlAPI.getStatus();

            // 1. 更新 UI 状态
            this.updateSpiUI(res);

            // 2. 处理日志
            if (res.logs) {
                this.appendLog(res.logs);
            }

            // 3. 如果发现服务停止了，自动停止轮询
            if (res.running === false && this.logTimer) {
                this.stopPolling();
                this.appendLog("[*] Monitor stopped.");
            }

        } catch (e) { console.error(e); }
    },

    updateSpiUI: function(status) {
        const dot = document.getElementById('spiStatusDot');
        const text = document.getElementById('spiStatusText');
        const info = document.getElementById('spiInfo');
        const btnStop = document.getElementById('btnStopSpi');
        const btnGenerate = document.getElementById('btnGenerateSnake');

        if (!dot) return;

        if (status && status.running) {
            dot.className = "status status-dot status-green status-dot-animated";
            text.innerText = "Running";
            text.className = "text-green";

            if(info) {
                info.innerHTML = `
                    <div class="mt-2">
                        <div><strong>Port:</strong> ${status.port}</div>
                        <div><strong>URL:</strong> ${status.url}</div>
                        <div class="text-truncate" title="${status.command}"><strong>CMD:</strong> ${status.command}</div>
                    </div>
                `;
            }
            if(btnStop) btnStop.disabled = false;
            if(btnGenerate) btnGenerate.disabled = true;
        } else {
            dot.className = "status status-dot status-secondary";
            text.innerText = "Stopped";
            text.className = "text-muted";
            if(info) info.innerHTML = '<div class="mt-2">Service is idle.</div>';
            if(btnStop) btnStop.disabled = true;
            if(btnGenerate) btnGenerate.disabled = false;
        }
    },

    stopServer: async function() {
        try {
            await window.SnakeYamlAPI.stop();
            await this.refreshStatus();
            this.appendLog("[!] Server stopped by user.");
        } catch (e) {
            this.appendLog("Stop failed: " + e.message);
        }
    },

    // --- 日志辅助 ---
    appendLog: function(msg) {
        const area = document.getElementById('snakeLogArea');
        if(!area) return;
        area.value += msg;
        area.scrollTop = area.scrollHeight;
    },

    clearLog: function() {
        const area = document.getElementById('snakeLogArea');
        if(area) area.value = '';
    },


    copyResult: function() {
        const area = document.getElementById('snakeResult');
        if(area && area.value) {
            area.select();
            document.execCommand('copy');
            const btn = document.getElementById('btnCopy');
            const origin = btn.innerHTML;
            btn.innerHTML = '<i class="bi bi-check"></i> Copied';
            setTimeout(() => btn.innerHTML = origin, 1500);
        }
    }
};