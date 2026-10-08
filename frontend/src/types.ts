export type Role = 'ADMIN' | 'MEMBER'
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'
export interface Project {
  id: number
  name: string
  description: string
  createdAt: string
  updatedAt: string
  role: Role
  memberCount: number
  taskCount: number
  doneCount: number
}
export interface ProjectInput {
  name: string
  description: string
}
export interface Member {
  id: number
  username: string
  displayName: string
  role: Role
}
export interface UserOption {
  id: number
  username: string
  displayName: string
}
export interface Task {
  id: number
  projectId: number
  title: string
  description: string
  status: TaskStatus
  priority: Priority
  creatorId: number
  assigneeId: number | null
  creatorName: string
  assigneeName: string | null
  dueDate: string | null
  version: number
  createdAt: string
  updatedAt: string
}
export interface TaskInput {
  title: string
  description: string
  status: TaskStatus
  priority: Priority
  assigneeId: number | null
  dueDate: string | null
  version?: number
}
export interface TaskComment {
  id: number
  taskId: number
  authorId: number
  authorName: string
  content: string
  createdAt: string
}
export interface Stats {
  total: number
  todo: number
  inProgress: number
  done: number
  overdue: number
  completionRate: number
}
export const statusNames: Record<TaskStatus, string> = {
  TODO: '待处理',
  IN_PROGRESS: '进行中',
  DONE: '已完成',
}
export const priorityNames: Record<Priority, string> = { LOW: '低', MEDIUM: '中', HIGH: '高' }
export function formatDate(value: string | null | undefined) {
  return value ? value.replace('T', ' ').slice(0, 16) : '未设置'
}
export function isOverdue(task: Task) {
  const today = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date())
  return !!task.dueDate && task.dueDate < today && task.status !== 'DONE'
}
