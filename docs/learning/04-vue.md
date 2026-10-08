# 04 · Vue把JSON显示成页面

[上一章](03-read-projects.md) · [目录](../LEARNING.md) · [下一章](05-create-project.md)

## 组件和入口

HTML结构、CSS样式、JS行为，Vue加上组件与响应式，让你描述数据和视图关系，而非反复手动改DOM。

.vue常见script setup逻辑、template结构、style样式；本项目大量CSS放assets/main.css，不是每个文件都必须style。

[main.ts](../../frontend/src/main.ts)createApp(App)，安装Pinia和Router，mount到[index.html](../../frontend/index.html)的#app。App.vue是外层布局，RouterView显示当前页面。

## ref和reactive

[ProjectsView](../../frontend/src/views/ProjectsView.vue)：

```ts
const projects = ref<Project[]>([])
```

空数组是初值，泛型说明元素类型。脚本用projects.value，模板顶层ref自动解包，写projects.length。普通变量赋值不保证视图响应。

reactive({title:''})适合一组字段，直接form.title；ref也能装对象，不是“对象只能reactive”。

## 请求状态不是装饰

loading显示正在加载，error显示失败和重试，成功无记录显示空状态，有记录显示卡片。这帮助用户区分无数据与服务坏了。

onMounted(load)组件挂载后加载。真实load包含请求序号：

```ts
const version = ++loadVersion
const data = await getProjects()
if (version === loadVersion) projects.value = data
```

这是节选，完整有try/catch/finally。最新请求才能更新界面，防止旧慢响应覆盖新结果。loadVersion不是数据库任务version。

## 请求封装

[api/projects.ts](../../frontend/src/api/projects.ts)：

```ts
export const getProjects = () => request<Project[]>('/api/projects')
```

[http.ts](../../frontend/src/api/http.ts)统一超时、Cookie、CSRF、错误与204。原生fetch收到404/500通常仍返回Response，所以要检查response.ok；response.json异步解析；204无体不能照常读JSON。泛型不做运行时数据校验。

## 模板逐项解释

```vue
<RouterLink v-for="project in filtered" :key="project.id" :to="'/projects/' + project.id">
  {{ project.name }}
</RouterLink>
```

真实组件相关结构：插值显示文本，v-for循环，key稳定标识，冒号动态属性绑定，RouterLink负责页面导航。key不能随便用数组位置，排序变化不应该改变业务身份。

v-if/v-else-if/v-else条件渲染，@click="load"点击事件，:disabled="loading"动态禁用。插值默认转义用户文本，不要随便用v-html显示评论，否则有XSS风险。

## computed：派生数据

filtered从projects和search计算过滤，totals用reduce累加计数。搜索项目是对已加载数据本地过滤，不是每输入字符查MySQL。任务筛选则调用后端，要分清。

能够算出的值通常不再手动存另一份，否则容易不同步。computed算值，watch更适合响应变化执行请求等副作用。

## 路由与共享状态

[router](../../frontend/src/router/index.ts)将/projects与/projects/:id映射页面，:id动态参数；()=>import延迟加载页面。

[auth store](../../frontend/src/stores/auth.ts)用Pinia共享用户，局部搜索用ref就够。刷新会清掉前端内存，/me从后端恢复认证状态。

## 练习与答案

在项目卡片加“更新时间：{{ project.updatedAt }}”，不改API和SQL。它已有字段；显示格式不友好可用types.ts的formatDate，不必改库。

为什么搜索没有Network请求？computed本地筛选。为什么模板没有.value？顶层ref自动解包。
