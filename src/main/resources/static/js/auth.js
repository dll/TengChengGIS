// ── 鉴权与请求拦截 ──（独立 IIFE，确保最先加载）
(function(){
    var TOKEN_KEY = 'tingcheng_token';
    var USER_KEY = 'tingcheng_user';

    function getToken() { return localStorage.getItem(TOKEN_KEY) || ''; }
    function getUser()  { try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); } catch(e) { return null; } }
    function setAuth(token, user) {
        localStorage.setItem(TOKEN_KEY, token || '');
        localStorage.setItem(USER_KEY, JSON.stringify(user || null));
        renderAuth();
    }
    function clearAuth() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        renderAuth();
    }

    function renderAuth() {
        var u = getUser();
        var $user = document.getElementById('authUser');
        var $login = document.getElementById('btnLogin');
        var $logout = document.getElementById('btnLogout');
        var $tabPwdLi = document.getElementById('tabPwdLi');
        if (!$user || !$login || !$logout) return;
        var isLoggedIn = u && getToken();
        if (isLoggedIn) {
            $user.style.display = '';
            $user.innerHTML = '👤 ' + (u.displayName || u.username) +
                ' (' + (u.role || 'USER') + ')' +
                ' <a href="javascript:showChangePwdDialog()" style="color:#f39c12;text-decoration:none;font-size:10px">🔑改密</a>';
            $login.style.display = 'none';
            $logout.style.display = '';
            if ($tabPwdLi) $tabPwdLi.style.display = '';
        } else {
            $user.style.display = '';
            $user.innerHTML = '未登录';
            $login.style.display = '';
            $logout.style.display = 'none';
            if ($tabPwdLi) $tabPwdLi.style.display = 'none';
        }
        // admin-only button visibility
        (document.querySelectorAll('.admin-only') || []).forEach(function(el){
            el.style.display = isLoggedIn && u && u.role === 'ADMIN' ? '' : 'none';
        });
    }

    // 包装原生 fetch：自动加 Authorization；遇 401 提示并弹登录框
    var rawFetch = window.fetch.bind(window);
    window.fetch = function(input, init) {
        init = init || {};
        var headers = new Headers(init.headers || (typeof input === 'object' ? input.headers : null) || {});
        var token = getToken();
        if (token && !headers.has('Authorization')) {
            headers.set('Authorization', 'Bearer ' + token);
        }
        init.headers = headers;
        return rawFetch(input, init).then(function(resp){
            if (resp.status === 401) {
                clearAuth();
                if (typeof window.showLoginDialog === 'function') {
                    setTimeout(function(){ window.showLoginDialog('登录已过期，请重新登录'); }, 0);
                }
            } else if (resp.status === 403) {
                if (typeof window.toast === 'function') window.toast('权限不足', 'warning');
            }
            return resp;
        });
    };

    function getModal() {
        var el = document.getElementById('loginModal');
        if (!el || !window.bootstrap) return null;
        return bootstrap.Modal.getOrCreateInstance(el);
    }

    window.showLoginDialog = function(message) {
        var msg = document.getElementById('loginMsg');
        if (msg) msg.textContent = message || '';
        var m = getModal();
        if (m) m.show();
    };

    window.showChangePwdDialog = function() {
        var el = document.getElementById('loginModal');
        if (!el) return;
        // switch to password tab
        var tab = document.querySelector('a[href="#tabPwdPane"]');
        if (tab && window.bootstrap) { var t = new bootstrap.Tab(tab); t.show(); }
        document.getElementById('pwdOld').value = '';
        document.getElementById('pwdNew').value = '';
        document.getElementById('pwdMsg').textContent = '';
        var m = getModal();
        if (m) m.show();
    };

    async function submitChangePassword() {
        var msg = document.getElementById('pwdMsg');
        msg.textContent = '';
        var oldPwd = document.getElementById('pwdOld').value || '';
        var newPwd = document.getElementById('pwdNew').value || '';
        if (!oldPwd || !newPwd) { msg.textContent = '请填写旧密码和新密码'; return; }
        if (newPwd.length < 4) { msg.textContent = '新密码长度不能少于4位'; return; }
        try {
            var resp = await rawFetch('/auth/change-password', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({oldPassword: oldPwd, newPassword: newPwd})
            });
            var data = await resp.json();
            if (data.success) {
                var m = getModal(); if (m) m.hide();
                if (typeof window.toast === 'function') window.toast('密码修改成功', 'success');
            } else {
                msg.textContent = data.message || '修改失败';
            }
        } catch (e) {
            msg.textContent = '网络错误：' + e.message;
        }
    }

    window.doLogout = function() {
        clearAuth();
        if (typeof window.toast === 'function') window.toast('已退出登录', 'info');
    };

    async function submitLogin() {
        var msg = document.getElementById('loginMsg');
        msg.textContent = '';
        var username = (document.getElementById('loginUsername').value || '').trim();
        var password = document.getElementById('loginPassword').value || '';
        if (!username || !password) { msg.textContent = '请输入账号与密码'; return; }
        try {
            var resp = await rawFetch('/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: username, password: password})
            });
            var data = await resp.json();
            if (resp.ok && data.success) {
                setAuth(data.token, {username: data.username, displayName: data.displayName, role: data.role});
                var m = getModal(); if (m) m.hide();
                if (typeof window.toast === 'function') window.toast('登录成功，欢迎 ' + (data.displayName || data.username), 'success');
            } else {
                msg.textContent = data.message || '登录失败';
            }
        } catch (e) {
            msg.textContent = '网络错误：' + e.message;
        }
    }

    async function submitRegister() {
        var msg = document.getElementById('regMsg');
        msg.textContent = '';
        var username = (document.getElementById('regUsername').value || '').trim();
        var displayName = (document.getElementById('regDisplay').value || '').trim();
        var password = document.getElementById('regPassword').value || '';
        if (!username || !password) { msg.textContent = '请输入账号与密码'; return; }
        try {
            var resp = await rawFetch('/auth/register', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: username, password: password, displayName: displayName})
            });
            var data = await resp.json();
            if (resp.ok && data.success) {
                setAuth(data.token, {username: data.username, displayName: data.displayName, role: data.role});
                var m = getModal(); if (m) m.hide();
                if (typeof window.toast === 'function') window.toast('注册成功，已自动登录', 'success');
            } else {
                msg.textContent = data.message || '注册失败';
            }
        } catch (e) {
            msg.textContent = '网络错误：' + e.message;
        }
    }

    document.addEventListener('DOMContentLoaded', function(){
        renderAuth();
        var btn = document.getElementById('btnSubmitAuth');
        if (btn) btn.addEventListener('click', function(){
            var active = document.querySelector('#loginModal .nav-link.active');
            if (!active) { submitLogin(); return; }
            var href = active.getAttribute('href');
            if (href === '#tabRegPane') submitRegister();
            else if (href === '#tabPwdPane') submitChangePassword();
            else submitLogin();
        });
        // 回车提交
        ['loginPassword','regPassword','pwdNew'].forEach(function(id){
            var el = document.getElementById(id);
            if (el) el.addEventListener('keydown', function(e){ if (e.key === 'Enter') document.getElementById('btnSubmitAuth').click(); });
        });
    });

    // 导出供测试
    window.renderAuth = renderAuth;
    window.__setAuth = setAuth;
    window.__clearAuth = clearAuth;
})();
