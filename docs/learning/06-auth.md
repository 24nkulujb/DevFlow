# 06 · 登录、Cookie、Session与CSRF

[上一章](05-create-project.md) · [目录](../LEARNING.md) · [下一章](07-permissions.md)

## 认证和授权

认证问“你是谁”，授权问“可以做什么”。Alice登录成功不代表能看Bob所有项目。本项目用Session，不是JWT，不要直接混入localStorage token教程。

## 登录全过程

```text
LoginView → auth store.login → loginApi → POST /api/auth/login
→ Security过滤器 → 查用户 → BCrypt验证 → 建立Session
→ 204 → GET /api/auth/me → Pinia保存用户 → 项目页
```

[api/auth.ts](../../frontend/src/api/auth.ts)用URLSearchParams编码username/password，Content-Type是application/x-www-form-urlencoded，不是JSON，因为[SecurityConfig](../../backend/src/main/java/com/devflow/backend/config/SecurityConfig.java)配置formLogin。

AuthController没有login方法不是遗漏，这个地址由Security过滤器在Controller前处理。

## 拆解密码加载代码

```java
return username -> {
    var user = userMapper.findByUsername(username);
    if (user == null) {
        throw new UsernameNotFoundException("用户名或密码错误");
    }
    return User.withUsername(user.username())
        .password(user.passwordHash())
        .roles("USER")
        .build();
};
```

这是SecurityConfig节选。lambda定义“给用户名，加载用户”的行为；var由编译器推断局部类型，不代表Java动态类型。链式调用逐步构建Security所需用户对象；USER是全局认证角色，不是项目ADMIN。

DaoAuthenticationProvider通过PasswordEncoder验证输入密码。本项目BCrypt加随机盐，相同密码多次encode结果可能不同；必须matches输入与已存哈希，不能重新encode后字符串比较。

哈希不可像加密那样解密，末尾.和/等字符是编码内容，不要剪掉。哈希泄露仍有离线猜密码风险，不应公开。

## Session和Cookie

后端Session保存认证状态，浏览器Cookie保存会话标识，通常为JSESSIONID。后续请求带标识，后端找到会话。Pinia用户只供页面显示，不是安全凭证；刷新后靠/me确认会话。

http.ts设置credentials:same-origin，通过Vite代理/api保持浏览器同源；主机名与端口影响来源，混用localhost/127.0.0.1可能影响Cookie/跨域。

HttpOnly防止普通JS读Cookie，不妨碍浏览器自动发送；SameSite限制部分跨站场景，不替代完整安全设计。生产Secure要求HTTPS，不能直接把生产配置搬到本地HTTP。

## CSRF是另一个问题

因为浏览器自动带Cookie，恶意网站可能诱导已登录用户发送写请求。CSRF token给写请求加额外校验，并不是密码或登录身份。

AuthController /csrf返回headerName/token；http.ts的csrfHeaders获取，request在非GET/HEAD/OPTIONS时设置。登录/退出会改变安全上下文，本版每次写请求重新取，避免旧token。

permitAll允许访问登录地址，不代表关闭CSRF。拿到token不代表获得项目权限。

## 前端守卫不是后端门锁

router.beforeEach先auth.refresh，未登录去登录页，已登录访问登录页去项目页。只是体验，攻击者可不经过Vue直接请求API。

真正认证由.anyRequest().authenticated保护，项目授权由Service。隐藏按钮同样不保证安全。

## 退出和失效

POST /api/auth/logout带CSRF，由Security返回204；store清user。后端重启使当前内存Session丢失，需重新登录正常。

受保护请求401时http.ts广播auth:expired，App清状态跳转；登录错误401则用notifyUnauthorized=false避免全局误处理，并显示用户名/密码错误。

## 练习与答案

Network看登录Content-Type、204、之后/me；Application→Cookies只观察会话Cookie是否存在，不复制公开其值。退出后请求项目接口应拒绝。

测试csrfIsRequired验证缺CSRF的写请求403，不要通过关闭防护“修复”。/me返回id/username/displayName而不是passwordHash，减少信息泄露。
