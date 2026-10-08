export class HttpError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message)
    this.name = 'HttpError'
  }
}
async function fetchWithTimeout(path: string, options: RequestInit = {}) {
  return fetch(path, { ...options, credentials: 'same-origin', signal: AbortSignal.timeout(15000) })
}
async function csrfHeaders(): Promise<Record<string, string>> {
  const response = await fetchWithTimeout('/api/auth/csrf', { cache: 'no-store' })
  if (!response.ok) throw new HttpError(response.status, '无法获取请求校验信息，请刷新后重试')
  const data: { headerName: string; token: string } = await response.json()
  return { [data.headerName]: data.token }
}
export async function request<T>(
  path: string,
  options: RequestInit = {},
  notifyUnauthorized = true,
): Promise<T> {
  const method = (options.method || 'GET').toUpperCase()
  const headers = new Headers(options.headers)
  let response: Response
  try {
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const csrf = await csrfHeaders()
      Object.entries(csrf).forEach(([name, value]) => headers.set(name, value))
    }
    if (options.body && !headers.has('Content-Type'))
      headers.set('Content-Type', 'application/json')
    response = await fetchWithTimeout(path, { ...options, headers, cache: 'no-store' })
  } catch (error) {
    if (error instanceof HttpError) throw error
    throw new HttpError(
      0,
      method === 'GET'
        ? '无法连接服务，请检查网络或后端是否启动'
        : '未能确认保存结果。请先刷新检查，再决定是否重试，避免重复提交。',
    )
  }
  if (!response.ok) {
    if (response.status === 401 && notifyUnauthorized)
      window.dispatchEvent(new Event('auth:expired'))
    const fallback: Record<number, string> = {
      400: '请检查填写内容',
      401: '登录已失效，请重新登录',
      403: '没有操作权限或请求校验已失效，请刷新后重试',
      404: '记录不存在或你无权访问',
      409: '数据已变化，请刷新后重试',
      429: '操作过于频繁，请稍后再试',
    }
    let message = fallback[response.status] || '服务暂时不可用，请稍后重试'
    try {
      const body: { message?: string; fields?: Record<string, string> } = await response.json()
      const fields = Object.values(body.fields || {})
      message = fields.length ? fields.join('；') : body.message || message
    } catch {
      /* Security responses may intentionally have no body. */
    }
    throw new HttpError(response.status, message)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
