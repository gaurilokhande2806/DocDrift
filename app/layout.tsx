import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'DocDrift - Documentation Decay Detector',
  description: 'An Intelligent Framework for Detecting and Measuring Documentation-Code Inconsistency in Evolving Software Systems',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
