import { NextResponse } from 'next/server';
import { ExtractedElement } from '@/lib/types';
import { evaluateConsistency } from '@/lib/engine/consistencyEngine';
import { computeHealthScore } from '@/lib/engine/scoringModule';

export async function POST(request: Request) {
  let body: any = {};
  try {
    body = await request.json();
  } catch (e) {
    // Default fallback
  }

  let codeElements: ExtractedElement[] = [];
  let docElements: ExtractedElement[] = [];

  // Dynamic input parsing if custom code/doc provided in upload modal
  if (body.javaCode || body.readme) {
    const javaCode = body.javaCode || '';
    const readme = body.readme || '';

    // Extract endpoint from Java code
    const epMatch = javaCode.match(/@RequestMapping\(["'](.*?)["']\)/i) || javaCode.match(/@PostMapping/i);
    const epPath = epMatch && epMatch[1] ? epMatch[1] : '/api/users';
    
    codeElements.push({
      id: 'c-custom-1',
      type: 'ENDPOINT',
      sourceFile: 'CustomController.java',
      lineNumber: 10,
      name: epPath,
      value: 'POST'
    });

    // Extract parameters from Java code
    const paramsMatch = Array.from(javaCode.matchAll(/@RequestParam\s+(?:\w+\s+)?(\w+)/g));
    paramsMatch.forEach((m, idx) => {
      codeElements.push({
        id: `c-param-${idx}`,
        type: 'PARAMETER',
        sourceFile: 'CustomController.java',
        lineNumber: 12 + idx,
        name: m[1],
        value: 'String'
      });
    });

    // Extract endpoint from Readme
    const docEpMatch = readme.match(/(POST|GET|PUT|DELETE)\s+(/[a-zA-Z0-9_/]+)/i);
    if (docEpMatch) {
      docElements.push({
        id: 'd-custom-1',
        type: 'ENDPOINT',
        sourceFile: 'README.md',
        lineNumber: 5,
        name: docEpMatch[2],
        value: docEpMatch[1].toUpperCase()
      });
    }

    // Extract parameters from Readme
    const docParamMatch = readme.match(/(?:parameters|accepts|params|fields)[:\s]+([a-zA-Z0-9_,\s]+)/i);
    if (docParamMatch) {
      const tokens = docParamMatch[1].split(/[\s,]+/);
      tokens.forEach((t, idx) => {
        if (t.trim() && t.trim().length > 1) {
          docElements.push({
            id: `d-param-${idx}`,
            type: 'PARAMETER',
            sourceFile: 'README.md',
            lineNumber: 6,
            name: t.trim(),
            value: 'string'
          });
        }
      });
    }
  } else {
    // Standard Demo project elements
    codeElements = [
      { id: 'c1', type: 'ENDPOINT', sourceFile: 'UserController.java', lineNumber: 15, name: '/api/users', value: 'POST' },
      { id: 'c2', type: 'ENDPOINT', sourceFile: 'UserController.java', lineNumber: 25, name: '/api/users/payments', value: 'GET' },
      { id: 'c3', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 16, name: 'name', value: 'String' },
      { id: 'c4', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 17, name: 'email', value: 'String' },
      { id: 'c5', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 18, name: 'phone', value: 'String' },
      { id: 'c6', type: 'PARAMETER', sourceFile: 'UserController.java', lineNumber: 19, name: 'department', value: 'String' },
      { id: 'c7', type: 'JAVA_VERSION', sourceFile: 'pom.xml', lineNumber: 10, name: 'java.version', value: '17' },
      { id: 'c8', type: 'CONFIG_KEY', sourceFile: 'application.properties', lineNumber: 1, name: 'server.port', value: '9090' },
    ];

    docElements = [
      { id: 'd1', type: 'ENDPOINT', sourceFile: 'README.md', lineNumber: 10, name: '/api/users', value: 'POST' },
      { id: 'd2', type: 'ENDPOINT', sourceFile: 'README.md', lineNumber: 15, name: '/api/products', value: 'GET' },
      { id: 'd3', type: 'PARAMETER', sourceFile: 'README.md', lineNumber: 11, name: 'name', value: 'string' },
      { id: 'd4', type: 'PARAMETER', sourceFile: 'README.md', lineNumber: 12, name: 'email', value: 'string' },
      { id: 'd5', type: 'JAVA_VERSION', sourceFile: 'README.md', lineNumber: 4, name: 'java.version', value: '11' },
      { id: 'd6', type: 'CONFIG_KEY', sourceFile: 'README.md', lineNumber: 7, name: 'server.port', value: '8080' },
    ];
  }

  const totalElements = codeElements.length + docElements.length;
  const findings = evaluateConsistency(codeElements, docElements);
  const report = computeHealthScore(totalElements, findings);

  return NextResponse.json({
    runId: Date.now(),
    projectId: body.projectId || 1,
    projectName: body.projectName || 'Demo Spring Boot Store',
    version: 'v1.0.0',
    startedAt: new Date().toISOString(),
    finishedAt: new Date().toISOString(),
    status: 'COMPLETED',
    ...report,
  });
}
