window.GadgetModule = {
    init: async function() {
        const select = document.getElementById('gadgetChain');
        if (!select) return;
        const cmdInput = document.getElementById('gadgetCommand');
        if (cmdInput) {
            if (!cmdInput.dataset.hasEnterListener) {
                cmdInput.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') { e.preventDefault(); this.generate(); }
                });
                cmdInput.dataset.hasEnterListener = "true";
            }
        }

        try {
            const types = await window.GadgetAPI.getTypes();
            select.innerHTML = '';
            if (types && types.length > 0) {
                types.forEach(type => select.add(new Option(type, type)));
                select.value = types[0];
                this.onSelectionChange();
            } else {
                select.innerHTML = '<option disabled>Load Failed</option>';
            }
        } catch (e) {
            console.error(e);
            select.innerHTML = '<option disabled>Network Error</option>';
        }
    },

    onSelectionChange: async function() {
        const val = document.getElementById('gadgetChain')?.value;
        const out = document.getElementById('gadgetOutput');
        if (!val || !out) return;

        out.value = `> Loading info...`;
        try {
            const text = await window.GadgetAPI.getHelp(val);
            out.value = text || "> No description.";
        } catch (e) { out.value = "> Error loading info."; }
    },

    generate: async function() {
        const out = document.getElementById('gadgetOutput');
        if (!out) return;
        const chainElem = document.getElementById('gadgetChain');
        const cmdElem = document.getElementById('gadgetCommand');
        const encElem = document.getElementById('encodingSelect');
        const dirtyElem = document.getElementById('dirtyDataCheck');

        const data = {
            gadget: chainElem?.value || '',
            command: cmdElem?.value || '',
            encoding: encElem?.value || 'base64', // 给个默认值防止报错
            dirtyData: dirtyElem?.checked || false
        };

        if (!data.command) {
            out.value = "> Please input command first...";
            if(cmdElem) cmdElem.focus();
            return;
        }

        out.value = "> Generating...";

        try {
            const res = await window.GadgetAPI.generate(data);
            out.value = res.success ? res.data : `> Error: ${res.msg}`;
        } catch (e) { out.value = `> System Error: ${e.message}`; }
    },

    copyResult: function() {
        const out = document.getElementById('gadgetOutput');
        if (!out || !out.value) return;

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(out.value).then(() => {
            }).catch(err => {
                console.error('Async: Could not copy text: ', err);
            });
        } else {
            out.select();
            document.execCommand('copy');
        }
    }
};