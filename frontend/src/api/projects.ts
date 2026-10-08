import { request } from './http'
import type {
  Project,
  ProjectInput,
  Member,
  Role,
  UserOption,
  Task,
  TaskInput,
  TaskStatus,
  TaskComment,
  Stats,
} from '../types'
export { HttpError } from './http'
export type { Project, ProjectInput as CreateProjectInput } from '../types'
const base = (id: number) => '/api/projects/' + id
export const getProjects = () => request<Project[]>('/api/projects')
export const createProjectApi = (input: ProjectInput) =>
  request<Project>('/api/projects', { method: 'POST', body: JSON.stringify(input) })
export const getProject = (id: number) => request<Project>(base(id))
export const updateProject = (id: number, input: ProjectInput) =>
  request<Project>(base(id), { method: 'PUT', body: JSON.stringify(input) })
export const getMembers = (id: number) => request<Member[]>(base(id) + '/members')
export const getCandidates = (id: number, q = '') =>
  request<UserOption[]>(base(id) + '/users?q=' + encodeURIComponent(q))
export const addMember = (id: number, userId: number, role: Role) =>
  request<Member[]>(base(id) + '/members', {
    method: 'POST',
    body: JSON.stringify({ userId, role }),
  })
export const changeRole = (id: number, userId: number, role: Role) =>
  request<Member[]>(base(id) + '/members/' + userId, {
    method: 'PATCH',
    body: JSON.stringify({ role }),
  })
export const removeMember = (id: number, userId: number) =>
  request<void>(base(id) + '/members/' + userId, { method: 'DELETE' })
export const getStats = (id: number) => request<Stats>(base(id) + '/stats')
export interface TaskFilters {
  status?: string
  priority?: string
  assigneeId?: string
  q?: string
  overdue?: boolean
}
export function getTasks(id: number, filters: TaskFilters = {}) {
  const query = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== '' && value !== false) query.set(key, String(value))
  })
  return request<Task[]>(base(id) + '/tasks?' + query)
}
export const getTask = (id: number, taskId: number) => request<Task>(base(id) + '/tasks/' + taskId)
export const createTask = (id: number, input: TaskInput) =>
  request<Task>(base(id) + '/tasks', { method: 'POST', body: JSON.stringify(input) })
export const updateTask = (id: number, taskId: number, input: TaskInput) =>
  request<Task>(base(id) + '/tasks/' + taskId, { method: 'PUT', body: JSON.stringify(input) })
export const updateTaskStatus = (id: number, task: Task, status: TaskStatus) =>
  request<Task>(base(id) + '/tasks/' + task.id + '/status', {
    method: 'PATCH',
    body: JSON.stringify({ status, version: task.version }),
  })
export const getComments = (id: number, taskId: number) =>
  request<TaskComment[]>(base(id) + '/tasks/' + taskId + '/comments')
export const addComment = (id: number, taskId: number, content: string) =>
  request<TaskComment[]>(base(id) + '/tasks/' + taskId + '/comments', {
    method: 'POST',
    body: JSON.stringify({ content }),
  })
