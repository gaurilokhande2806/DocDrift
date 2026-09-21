import { NextResponse } from 'next/server';

export async function GET() {
  const markdown = `# DocDrift Report: Demo Spring Boot Store

**DOCUMENTATION HEALTH SCORE**: 87.5 / 100 (MILD DECAY)

## Analysis Summary
- **Project Name**: Demo Spring Boot Store
- **Version**: v1.0.0
- **Total Verifiable Elements**: 14
- **Total Inconsistencies Detected**: 4

## Findings Breakdown

### Finding #1 | DC-02 Removed API | HIGH
- **Doc Location**: \`README.md:15\`
- **Code Location**: \`N/A\`
- **Documented Value**: GET /api/products
- **Actual Value**: Endpoint removed from code
- **Difference**: - /api/products
- **Suggestion**: Remove endpoint /api/products from documentation

### Finding #2 | DC-03 Undocumented API | MEDIUM
- **Doc Location**: \`N/A\`
- **Code Location**: \`UserController.java:25\`
- **Documented Value**: No documentation found
- **Actual Value**: GET /api/users/payments exists in code
- **Difference**: + /api/users/payments
- **Suggestion**: Add documentation entry for GET /api/users/payments

### Finding #3 | DC-01 Parameter Mismatch | MEDIUM
- **Doc Location**: \`README.md\`
- **Code Location**: \`UserController.java:12\`
- **Documented Value**: Documented params: name, email
- **Actual Value**: Implemented params: name, email, phone, department
- **Difference**: + phone, department missing from docs
- **Suggestion**: Add missing request parameters (phone, department) to documentation schema

### Finding #4 | DC-08 README Version Drift | MEDIUM
- **Doc Location**: \`README.md:4\`
- **Code Location**: \`pom.xml:10\`
- **Documented Value**: Java 11
- **Actual Value**: Java 17
- **Difference**: Version mismatch: Java 11 vs Java 17
- **Suggestion**: Update README prerequisites to specify Java 17
`;

  return new NextResponse(markdown, {
    headers: {
      'Content-Type': 'text/markdown',
      'Content-Disposition': 'attachment; filename="docdrift-report.md"',
    },
  });
}
