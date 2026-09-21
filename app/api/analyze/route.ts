import { NextResponse } from 'next/server';
import { ExtractedElement } from '@/lib/types';
import { evaluateConsistency } from '@/lib/engine/consistencyEngine';
import { computeHealthScore } from '@/lib/engine/scoringModule';

export async function POST() {
  // Extracted elements from demo project
  const codeElements: ExtractedElement[] = [
    { id: 'c1', type: 'ENDPOINT', sourceFile: 'UserController.java', lineNumber: 15, name: '/api/users', value: 'POST' },
    { id: 'c2', type: 'ENDPOINT', sourceFile: 'UserController.java', lineNumber: 25, name: '/api/users/payments', value: 'GET' },
    { id: 'c3', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 16, name: 'name', value: 'String' },
    { id: 'c4', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 17, name: 'email', value: 'String' },
    { id: 'c5', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 18, name: 'phone', value: 'String' },
    { id: 'c6', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 19, name: 'department', value: 'String' },
    { id: 'c7', type: 'JAVA_VERSION', sourceFile: 'pom.xml', lineNumber: 10, name: 'java.version', value: '17' },
    { id: 'c8', type: 'CONFIG_KEY', sourceFile: 'application.properties', lineNumber: 1, name: 'server.port', value: '9090' },
  ];

  const docElements: ExtractedElement[] = [
    { id: 'd1', type: 'ENDPOINT', sourceFile: 'README.md', lineNumber: 10, name: '/api/users', value: 'POST' },
    { id: 'd2', type: 'ENDPOINT', sourceFile: 'README.md', lineNumber: 15, name: '/api/products', value: 'GET' },
    { id: 'd3', type: 'PARAMETER', sourceFile: 'README.md', lineNumber: 11, name: 'name', value: 'string' },
    { id: 'd4', type: 'PARAMETER', sourceFile: 'README.md', lineNumber: 12, name: 'email', value: 'string' },
    { id: 'd5', type: 'JAVA_VERSION', sourceFile: 'README.md', lineNumber: 4, name: 'java.version', value: '11' },
    { id: 'd6', type: 'CONFIG_KEY', sourceFile: 'README.md', lineNumber: 7, name: 'server.port', value: '8080' },
  ];

  const totalElements = codeElements.length + docElements.length;
  const findings = evaluateConsistency(codeElements, docElements);
  const report = computeHealthScore(totalElements, findings);

  return NextResponse.json({
    runId: Date.now(),
    projectId: 1,
    projectName: 'Demo Spring Boot Store',
    version: 'v1.0.0',
    startedAt: new Date().toISOString(),
    finishedAt: new Date().toISOString(),
    status: 'COMPLETED',
    ...report,
  });
}
