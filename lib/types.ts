export type Category = 
  | 'DC-01' // Parameter Mismatch
  | 'DC-02' // Removed API
  | 'DC-03' // Undocumented API
  | 'DC-04' // Response Mismatch
  | 'DC-05' // Method Mismatch
  | 'DC-06' // Database Drift
  | 'DC-07' // Javadoc Mismatch
  | 'DC-08' // README Version Drift
  | 'DC-09' // Config Mismatch
  | 'DC-10'; // Terminology Error

export type Severity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type DecayLevel = 'HEALTHY' | 'MILD DECAY' | 'MODERATE DECAY' | 'SEVERE DECAY';

export interface ExtractedElement {
  id: string;
  type: 'ENDPOINT' | 'PARAMETER' | 'RESPONSE_FIELD' | 'JAVADOC_TAG' | 'DB_COLUMN' | 'JAVA_VERSION' | 'CONFIG_KEY';
  sourceFile: string;
  lineNumber: number;
  name: string;
  value: string;
  parentName?: string;
  metadata?: Record<string, string>;
}

export interface Finding {
  id: string;
  category: Category;
  severity: Severity;
  docLocation: string;
  codeLocation: string;
  documentedValue: string;
  actualValue: string;
  difference: string;
  suggestion: string;
  confidence: number;
  status: 'OPEN' | 'FALSE_POSITIVE' | 'ACCEPTED';
  firstSeenVersion: string;
}

export interface HealthScoreReport {
  overallDhs: number;
  decayLevel: DecayLevel;
  totalVerifiableElements: number;
  totalFindings: number;
  weightedSum: number;
  categoryScores: Record<string, number>;
  findings: Finding[];
}
