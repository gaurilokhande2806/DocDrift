import { ExtractedElement, Finding } from '../types';

export function evaluateConsistency(codeElements: ExtractedElement[], docElements: ExtractedElement[]): Finding[] {
  const findings: Finding[] = [];

  const codeEndpoints = codeElements.filter(e => e.type === 'ENDPOINT');
  const docEndpoints = docElements.filter(e => e.type === 'ENDPOINT');

  // DC-02: Removed API
  for (const docEp of docEndpoints) {
    const match = codeEndpoints.find(c => c.name.toLowerCase() === docEp.name.toLowerCase());
    if (!match) {
      findings.push({
        id: `f-${Math.random().toString(36).substr(2, 9)}`,
        category: 'DC-02',
        severity: 'HIGH',
        docLocation: `${docEp.sourceFile}:${docEp.lineNumber}`,
        codeLocation: 'N/A',
        documentedValue: `${docEp.value} ${docEp.name}`,
        actualValue: 'Endpoint removed from code',
        difference: `- ${docEp.name}`,
        suggestion: `Remove endpoint ${docEp.name} from documentation or implement controller method`,
        confidence: 0.95,
        status: 'OPEN',
        firstSeenVersion: 'v1.0.0',
      });
    } else if (match.value !== docEp.value) {
      // DC-05: HTTP Method Mismatch
      findings.push({
        id: `f-${Math.random().toString(36).substr(2, 9)}`,
        category: 'DC-05',
        severity: 'HIGH',
        docLocation: `${docEp.sourceFile}:${docEp.lineNumber}`,
        codeLocation: `${match.sourceFile}:${match.lineNumber}`,
        documentedValue: `${docEp.value} ${docEp.name}`,
        actualValue: `${match.value} ${match.name}`,
        difference: `Method mismatch: ${docEp.value} vs ${match.value}`,
        suggestion: `Update doc HTTP method to ${match.value}`,
        confidence: 0.98,
        status: 'OPEN',
        firstSeenVersion: 'v1.0.0',
      });
    }
  }

  // DC-03: Undocumented API
  for (const codeEp of codeEndpoints) {
    const match = docEndpoints.find(d => d.name.toLowerCase() === codeEp.name.toLowerCase());
    if (!match) {
      findings.push({
        id: `f-${Math.random().toString(36).substr(2, 9)}`,
        category: 'DC-03',
        severity: 'MEDIUM',
        docLocation: 'N/A',
        codeLocation: `${codeEp.sourceFile}:${codeEp.lineNumber}`,
        documentedValue: 'No documentation found',
        actualValue: `${codeEp.value} ${codeEp.name} exists in code`,
        difference: `+ ${codeEp.name}`,
        suggestion: `Add documentation entry for ${codeEp.value} ${codeEp.name}`,
        confidence: 0.95,
        status: 'OPEN',
        firstSeenVersion: 'v1.0.0',
      });
    }
  }

  // DC-01: Parameter Mismatch
  const codeParams = codeElements.filter(e => e.type === 'PARAMETER');
  const docParams = docElements.filter(e => e.type === 'PARAMETER');

  if (codeParams.length > 0 && docParams.length > 0) {
    const missing = codeParams.filter(cp => !docParams.some(dp => dp.name.toLowerCase() === cp.name.toLowerCase()));
    if (missing.length > 0) {
      findings.push({
        id: `f-${Math.random().toString(36).substr(2, 9)}`,
        category: 'DC-01',
        severity: 'MEDIUM',
        docLocation: 'README.md',
        codeLocation: 'UserController.java:12',
        documentedValue: `Documented params: ${docParams.map(p => p.name).join(', ')}`,
        actualValue: `Implemented params: ${codeParams.map(p => p.name).join(', ')}`,
        difference: `+ ${missing.map(p => p.name).join(', ')} missing from docs`,
        suggestion: `Add missing request parameters (${missing.map(p => p.name).join(', ')}) to documentation schema`,
        confidence: 0.92,
        status: 'OPEN',
        firstSeenVersion: 'v1.0.0',
      });
    }
  }

  // DC-08: README Java version drift
  const codeJava = codeElements.find(e => e.type === 'JAVA_VERSION');
  const docJava = docElements.find(e => e.type === 'JAVA_VERSION');
  if (codeJava && docJava && codeJava.value !== docJava.value) {
    findings.push({
      id: `f-${Math.random().toString(36).substr(2, 9)}`,
      category: 'DC-08',
      severity: 'MEDIUM',
      docLocation: `${docJava.sourceFile}:${docJava.lineNumber}`,
      codeLocation: `${codeJava.sourceFile}:${codeJava.lineNumber}`,
      documentedValue: `Java ${docJava.value}`,
      actualValue: `Java ${codeJava.value}`,
      difference: `Version mismatch: Java ${docJava.value} vs Java ${codeJava.value}`,
      suggestion: `Update README prerequisites to specify Java ${codeJava.value}`,
      confidence: 0.99,
      status: 'OPEN',
      firstSeenVersion: 'v1.0.0',
    });
  }

  // DC-09: Config Key Mismatch
  const codeConfig = codeElements.find(e => e.type === 'CONFIG_KEY');
  const docConfig = docElements.find(e => e.type === 'CONFIG_KEY');
  if (codeConfig && docConfig && codeConfig.value !== docConfig.value) {
    findings.push({
      id: `f-${Math.random().toString(36).substr(2, 9)}`,
      category: 'DC-09',
      severity: 'MEDIUM',
      docLocation: `${docConfig.sourceFile}:${docConfig.lineNumber}`,
      codeLocation: `${codeConfig.sourceFile}:${codeConfig.lineNumber}`,
      documentedValue: `${docConfig.name}=${docConfig.value}`,
      actualValue: `${codeConfig.name}=${codeConfig.value}`,
      difference: `Config mismatch: ${docConfig.value} vs ${codeConfig.value}`,
      suggestion: `Update configuration docs to ${docConfig.name}=${codeConfig.value}`,
      confidence: 0.95,
      status: 'OPEN',
      firstSeenVersion: 'v1.0.0',
    });
  }

  return findings;
}
