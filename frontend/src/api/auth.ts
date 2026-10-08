export interface CurrentUser {
    id: number
    username: string
    displayName: string
}

interface CsrfResponse {
    headerName: string
    token: string
}

export async function getCsrfHeaders(): Promise<Record<string, string>> {
  const response = await fetch('/api/auth/csrf', {
    cache: 'no-store',
  })

  if (!response.ok) {
    throw new Error('获取请求校验信息失败，请刷新页面')
  }

  const csrf: CsrfResponse = await response.json()

  return { [csrf.headerName]: csrf.token }
}

export async function getCurrentUser(): Promise<CurrentUser | null> {
  const response = await fetch('/api/auth/me', {
    cache: 'no-store',
  })

  if (response.status === 401) return null

  if (!response.ok) {
    throw new Error('获取当前用户失败')
  }

  return response.json()
}

export async function loginApi(
  username: string,
  password: string,
): Promise<void> {
  const csrfHeaders = await getCsrfHeaders()

  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      ...csrfHeaders,
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({ username, password }),
  })

  if (!response.ok) {
    throw new Error(
      response.status === 401
        ? '用户名或密码错误'
        : `登录失败，状态码：${response.status}`,
    )
  }
}

export async function logoutApi(): Promise<void> {
  const csrfHeaders = await getCsrfHeaders()

  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    headers: csrfHeaders,
  })

  if (!response.ok) {
    throw new Error('退出失败，请重试')
  }
}