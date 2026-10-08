import assert from 'node:assert/strict'
const base=process.env.DEVFLOW_URL||'http://127.0.0.1:8080'
class Client {
 constructor(){this.cookies=new Map()}
 async fetch(path,options={}){
  const headers=new Headers(options.headers);headers.set('Cookie',[...this.cookies].map(([k,v])=>k+'='+v).join('; '))
  const r=await fetch(base+path,{...options,headers})
  for(const cookie of r.headers.getSetCookie()){const pair=cookie.split(';')[0];const i=pair.indexOf('=');this.cookies.set(pair.slice(0,i),pair.slice(i+1))}
  return r
 }
 async call(path,method='GET',body,expected=200){
  const headers={}
  if(method!=='GET'){const token=await this.fetch('/api/auth/csrf');assert.equal(token.status,200);const csrf=await token.json();headers[csrf.headerName]=csrf.token}
  if(body!==undefined)headers['Content-Type']='application/json'
  const r=await this.fetch(path,{method,headers,body:body===undefined?undefined:JSON.stringify(body)})
  assert.equal(r.status,expected,method+' '+path)
  if(expected===204||!r.headers.get('content-type')?.includes('json'))return null
  return r.json()
 }
 async login(name,password='DevFlow123!',expected=204){
  const token=await (await this.fetch('/api/auth/csrf')).json()
  const r=await this.fetch('/api/auth/login',{method:'POST',headers:{[token.headerName]:token.token,'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({username:name,password})})
  assert.equal(r.status,expected,'login '+name)
 }
}
const alice=new Client(),bob=new Client(),charlie=new Client(),anonymous=new Client()
await anonymous.call('/api/projects','GET',undefined,401)
await alice.login('alice','wrong',401);await alice.login('alice');await bob.login('bob');await charlie.login('charlie')
const me=await alice.call('/api/auth/me');assert.ok(!('passwordHash'in me))
const p=await alice.call('/api/projects','POST',{name:'第一版验收 · '+new Date().toISOString().slice(0,16),description:'自动验收生成的独立项目，保留作为演示。'},201)
const path='/api/projects/'+p.id
assert.equal(p.role,'ADMIN')
await bob.call(path,'GET',undefined,404)
await alice.call(path+'/members','POST',{userId:(await bob.call('/api/auth/me')).id,role:'MEMBER'},201)
await bob.call(path,'GET')
await bob.call(path,'PUT',{name:'越权改名'},403)
await alice.call(path,'PUT',{name:p.name,description:'已验证项目隔离、成员权限、任务版本和评论。'})
await alice.call(path+'/members/'+me.id,'DELETE',undefined,400)
await alice.call(path+'/members/'+me.id,'PATCH',{role:'MEMBER'},409)
const bobId=(await bob.call('/api/auth/me')).id
const parts=Object.fromEntries(new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(new Date()).map(p=>[p.type,p.value]))
const yesterday=new Date(parts.year+'-'+parts.month+'-'+parts.day+'T00:00:00Z');yesterday.setUTCDate(yesterday.getUTCDate()-1)
const input={title:'真实 MySQL 验收任务',description:'验证创建、状态、版本冲突与评论。',status:'TODO',priority:'HIGH',assigneeId:bobId,dueDate:yesterday.toISOString().slice(0,10)}
const task=await alice.call(path+'/tasks','POST',input,201)
await charlie.call(path+'/tasks/'+task.id,'GET',undefined,404)
const progress=await bob.call(path+'/tasks/'+task.id+'/status','PATCH',{status:'IN_PROGRESS',version:task.version})
await alice.call(path+'/tasks/'+task.id+'/status','PATCH',{status:'DONE',version:task.version},409)
const comment=await bob.call(path+'/tasks/'+task.id+'/comments','POST',{content:'评论与协作链路已通过。'},201)
assert.equal(comment.length,1)
await bob.call(path+'/tasks/'+task.id+'/comments','POST',{content:' '},400)
const done=await alice.call(path+'/tasks','POST',{...input,title:'已完成任务保留历史负责人'},201)
await bob.call(path+'/tasks/'+done.id+'/status','PATCH',{status:'DONE',version:done.version})
const stats=await alice.call(path+'/stats');assert.equal(stats.total,2);assert.equal(stats.overdue,1);assert.equal(stats.completionRate,50)
await alice.call(path+'/members/'+bobId,'DELETE',undefined,204)
assert.equal((await alice.call(path+'/tasks/'+progress.id)).assigneeId,null)
assert.equal((await alice.call(path+'/tasks/'+done.id)).assigneeId,bobId)
await bob.call(path,'GET',undefined,404)
await alice.call('/api/auth/logout','POST',undefined,204);await alice.call('/api/auth/me','GET',undefined,401)
console.log('PASS: MySQL + HTTP smoke test (project #'+p.id+')')
