window.FastjsonModule = {
    init: function() {
        if (!document.getElementById('fastjsonType')) return;
        const inputs = ['fastjsonCommand', 'jndiUrl'];
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
        this.loadTypes();
    },

    loadTypes: async function() {
        const s = document.getElementById('fastjsonType');
        try {
            const types = await window.FastjsonAPI.getTypes();
            s.innerHTML = '';
            types.forEach(t => s.add(new Option(t, t)));
            this.onTypeChange();
        } catch (e) {
            console.error(e);
            const out = document.getElementById('fastjsonOutput');
            if(out) out.value = "[!] Failed to load types.";
        }
    },

    onTypeChange: function() {
        const type = document.getElementById('fastjsonType').value;
        const cmdGroup = document.getElementById('commandGroup');
        const jndiGroup = document.getElementById('jndiGroup');
        const cmdLabel = document.querySelector('#commandGroup label');
        const cmdInput = document.getElementById('fastjsonCommand');
        const echoGroup = document.getElementById('echoGroup');

        const isTemplates = type === "TemplatesImpl";
        const isHex = type === "C3P0(Hex)";
        const isDns = type.startsWith("DNS");

        if (isTemplates || isHex || isDns) {
            cmdGroup.style.display = 'block';
            jndiGroup.style.display = 'none';
            echoGroup.style.display = isTemplates ? 'block' : 'none';

            if (isTemplates) {
                cmdLabel.innerText = "System Command";
                this.onEchoChange(); // 触发检查，决定是否禁用输入框
            } else if (isHex) {
                cmdLabel.innerText = "Serialized Hex String";
                cmdInput.placeholder = "aced0005...";
                cmdInput.disabled = false; // 非 Templates 模式，必须能输入
            } else if (isDns) {
                cmdLabel.innerText = "DNS Log Domain";
                cmdInput.placeholder = "xxx.dnslog.cn";
                cmdInput.disabled = false;
            }
        } else {
            cmdGroup.style.display = 'none';
            echoGroup.style.display = 'none';
            jndiGroup.style.display = 'block';
        }
    },

    onEchoChange: function() {
        const echoVal = document.getElementById('fastjsonEcho').value;
        const cmdInput = document.getElementById('fastjsonCommand');
        const echoHint = document.getElementById('echoHint');

        if (echoVal !== "None") {
            // 【核心修复】：选择回显时，强行变灰禁用，清空输入框
            cmdInput.disabled = true;
            cmdInput.value = "";
            cmdInput.placeholder = "[Ignored in Echo Mode]";
            echoHint.innerHTML = "Send request with header: <strong>X-Token: your_command</strong>";
            echoHint.style.display = 'block';
        } else {
            // 选择 None 时，恢复正常输入
            cmdInput.disabled = false;
            cmdInput.placeholder = "calc";
            echoHint.style.display = 'none';
        }
    },

    generate: async function() {
        const outputBox = document.getElementById('fastjsonOutput');

        const echoElem = document.getElementById('fastjsonEcho');
        const echoVal = echoElem ? echoElem.value : "None";

        const data = {
            type: document.getElementById('fastjsonType').value,
            command: document.getElementById('fastjsonCommand').value,
            jndiUrl: document.getElementById('jndiUrl').value,
            echoType: echoVal
        };

        const isCmdMode = document.getElementById('commandGroup').style.display !== 'none';

        // 前端拦截：如果是 None 且没填 Command，则拦截
        if (isCmdMode && echoVal === "None" && !data.command) {
            alert("Please input the required value (Command/DNS/Hex).");
            return;
        }

        if (!isCmdMode && !data.jndiUrl) {
            alert("Please input JNDI URL.");
            return;
        }

        outputBox.value = "[*] Generating payload...";

        try {
            const res = await window.FastjsonAPI.generate(data);
            if(res.success) {
                outputBox.value = res.data;
            } else {
                outputBox.value = `[!] Error: ${res.msg}`;
            }
        } catch (e) {
            outputBox.value = `[!] System Error: ${e.message}`;
        }
    },

    copyResult: function() {
        const out = document.getElementById('fastjsonOutput');
        if(out && out.value) {
            out.select();
            document.execCommand('copy');
            const btn = event.currentTarget;
            const org = btn.innerHTML;
            btn.innerHTML = '<i class="bi bi-check"></i> Copied';
            setTimeout(() => btn.innerHTML = org, 1000);
        }
    }
};