import { request, HttpError } from './http'
export interface CurrentUser {
  id: number
  username: string
  displayName: string
}
export async function getCurrentUser(): Promise<CurrentUser | null> {
  try {
    return await request<CurrentUser>('/api/auth/me', {}, false)
  } catch (error) {
    if (error instanceof HttpError && error.status === 401) return null
    throw error
  }
}
export async function loginApi(username: string, password: string) {
  try {
    await request<void>(
      '/api/auth/login',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ username, password }),
      },
      false,
    )
  } catch (error) {
    if (error instanceof HttpError && error.status === 401) throw new Error('用户名或密码错误')
    throw error
  }
}
export async function logoutApi() {
  await request<void>('/api/auth/logout', { method: 'POST' })
}
