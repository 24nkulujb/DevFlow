export interface Project {
    id: number
    name: string
    description: string
}

export interface CreateProjectInput {
    name: string
    description: string
}

export class HttpError extends Error {
    constructor(public readonly status:number) {
        super(`请求失败，状态码：${status}`)
        this.name = 'HttpError'
    }
}

export async function getProjects(): Promise<Project[]> {
    const response = await fetch('/api/projects')

    if (!response.ok) {
        throw new HttpError(response.status)
    }

    return response.json()
}

export async function createProjectApi(
  input: CreateProjectInput,
): Promise<void> {
  const response = await fetch('/api/projects', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(input),
  })

  if (!response.ok) {
    throw new HttpError(response.status)
  }
}