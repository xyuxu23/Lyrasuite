(function () {
    // 1. 定义 HTML 中调用的 setAppTheme 函数，并挂载到 window 对象
    window.setAppTheme = function(theme) {
        // 保存设置到本地存储
        localStorage.setItem('tablerTheme', theme);

        // 设置 HTML 标签属性
        document.documentElement.setAttribute('data-bs-theme', theme);

        // 确保 Body 也设置
        if (document.body) {
            document.body.setAttribute('data-bs-theme', theme);
        }
    };

    // 2. 初始化逻辑
    var storedTheme = localStorage.getItem('tablerTheme');

    // 如果本地没有存过，检测系统偏好
    if (!storedTheme) {
        storedTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    // 3. 应用主题
    window.setAppTheme(storedTheme);

})();