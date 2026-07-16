window.CodecModule = {
    init: function() {
        const input = document.getElementById('codecInput');
        if (!input) return;
        if (!input.dataset.hasShortcutListener) {
            input.addEventListener('keydown', (e) => {
                if (e.ctrlKey && e.key === 'Enter') {
                    e.preventDefault();
                    this.encode();
                }
            });
            input.dataset.hasShortcutListener = "true";
        }
    },

    encode: function() {
        this.run('encode');
    },

    decode: function() {
        this.run('decode');
    },

    run: function(mode) {
        const type = document.getElementById('codecType')?.value;
        const input = document.getElementById('codecInput')?.value || '';
        const output = document.getElementById('codecOutput');
        if (!output) return;

        try {
            if (mode === 'encode') {
                output.value = this.encodeByType(type, input);
            } else {
                output.value = this.decodeByType(type, input);
            }
        } catch (e) {
            output.value = `[!] ${e.message}`;
        }
    },

    encodeByType: function(type, value) {
        switch (type) {
            case 'base64':
                return btoa(unescape(encodeURIComponent(value)));
            case 'url':
                return encodeURIComponent(value);
            case 'unicode':
                return Array.from(value).map(ch => {
                    const code = ch.codePointAt(0).toString(16).padStart(4, '0');
                    return code.length > 4 ? `\\u{${code}}` : `\\u${code}`;
                }).join('');
            case 'hex':
                return Array.from(new TextEncoder().encode(value)).map(b => b.toString(16).padStart(2, '0')).join('');
            case 'html':
                return value.replace(/[&<>'"]/g, ch => ({
                    '&': '&amp;',
                    '<': '&lt;',
                    '>': '&gt;',
                    "'": '&#39;',
                    '"': '&quot;'
                })[ch]);
            default:
                return value;
        }
    },

    decodeByType: function(type, value) {
        switch (type) {
            case 'base64':
                return decodeURIComponent(escape(atob(value.replace(/\s+/g, ''))));
            case 'url':
                return decodeURIComponent(value);
            case 'unicode':
                return value
                    .replace(/\\u\{([0-9a-fA-F]+)\}/g, (_, hex) => String.fromCodePoint(parseInt(hex, 16)))
                    .replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) => String.fromCharCode(parseInt(hex, 16)));
            case 'hex': {
                const clean = value.replace(/[^0-9a-fA-F]/g, '');
                if (clean.length % 2 !== 0) throw new Error('Invalid hex length');
                const bytes = new Uint8Array(clean.match(/.{2}/g).map(b => parseInt(b, 16)));
                return new TextDecoder().decode(bytes);
            }
            case 'html': {
                const textarea = document.createElement('textarea');
                textarea.innerHTML = value;
                return textarea.value;
            }
            default:
                return value;
        }
    },

    swap: function() {
        const input = document.getElementById('codecInput');
        const output = document.getElementById('codecOutput');
        if (!input || !output) return;
        const temp = input.value;
        input.value = output.value;
        output.value = temp;
    },

    clear: function() {
        const input = document.getElementById('codecInput');
        const output = document.getElementById('codecOutput');
        if (input) input.value = '';
        if (output) output.value = '';
    },

    copyResult: function() {
        const out = document.getElementById('codecOutput');
        if (!out || !out.value) return;
        out.select();
        document.execCommand('copy');
        const btn = event.currentTarget;
        const org = btn.innerHTML;
        btn.innerHTML = '<i class="bi bi-check"></i> Copied';
        setTimeout(() => btn.innerHTML = org, 1000);
    }
};
