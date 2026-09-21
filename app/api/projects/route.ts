import { NextResponse } from 'next/server';

export async function GET() {
  const projects = [
    {
      id: 1,
      name: 'Demo Spring Boot Store',
      rootPath: 'samples/demo-project',
      description: 'Sample Spring Boot project with intentional documentation decay.',
      createdAt: new Date().toISOString(),
    }
  ];
  return NextResponse.json(projects);
}

export async function POST(request: Request) {
  const body = await request.json();
  const newProject = {
    id: Date.now(),
    name: body.name || 'New Project',
    rootPath: body.rootPath || './',
    description: body.description || '',
    createdAt: new Date().toISOString(),
  };
  return NextResponse.json(newProject);
}
