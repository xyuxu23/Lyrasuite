window.http = axios.create({
    baseURL: '/api',
    timeout: 10000
});

window.http.interceptors.response.use(response => {
    const res = response.data;
    if (res && res.hasOwnProperty('code') && res.hasOwnProperty('data')) {
        return res.data;
    }
    return res;
}, error => {
    console.error("API Error:", error);
    return Promise.reject(error);
});

window.GadgetAPI = {
    getTypes: () => window.http.get('/gadget/types'),
    getHelp: (name) => window.http.get(`/gadget/help?name=${name}`),
    generate: (data) => window.http.post('/gadget/generate', data)
};

window.JndiAPI = {
    getMeta: () => window.http.get('/jndi/meta'),
    start: (data) => window.http.post('/jndi/start', data),
    stop: (id) => window.http.post('/jndi/stop', {taskId: id}),
    getStatus: (id) => window.http.get(`/jndi/status?taskId=${id}`)
};

window.FastjsonAPI = {
    getTypes: () => window.http.get('/fastjson/types'),
    generate: (data) => window.http.post('/fastjson/generate', data)
};

window.ShiroAPI = {

    getGadgets: () => window.http.get('/shiro/gadgets'),
    getEchos: () => window.http.get('/shiro/echos'),
    start: (data) => window.http.post('/shiro/start', data),
    status: (taskId) => window.http.get(`/shiro/status?taskId=${taskId}`),
    stop: (taskId) => window.http.post('/shiro/stop', { taskId: taskId }),
    exec: (data) => window.http.post('/shiro/exec', data)
};

window.SnakeYamlAPI = {
    getTypes: () => window.http.get('/snakeyaml/types'),
    generate: (data) => window.http.post('/snakeyaml/generate', data),
    getStatus: () => window.http.get('/snakeyaml/status'),
    stop: () => window.http.post('/snakeyaml/stop')
};