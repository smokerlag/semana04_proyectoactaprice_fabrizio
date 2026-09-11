param(
  [Parameter(Mandatory = $true)][string]$DocxPath,
  [Parameter(Mandatory = $true)][string]$PdfPath
)
$ErrorActionPreference = "Stop"
if (-not (Test-Path $DocxPath)) { throw "DOCX no existe: $DocxPath" }
$docx = (Resolve-Path $DocxPath).Path
$pdf = $PdfPath
$pdfDir = Split-Path $pdf -Parent
if ($pdfDir -and -not (Test-Path $pdfDir)) { New-Item -ItemType Directory -Force -Path $pdfDir | Out-Null }
if (Test-Path $pdf) { Remove-Item $pdf -Force }

$word = $null
$doc = $null
try {
  $word = New-Object -ComObject Word.Application
  $word.Visible = $false
  $word.DisplayAlerts = 0
  $doc = $word.Documents.Open($docx)
  # 17 = wdFormatPDF
  $null = $doc.SaveAs([ref]$pdf, [ref]17)
  Write-Output "OK $pdf"
} finally {
  if ($doc -ne $null) { $doc.Close($false) | Out-Null }
  if ($word -ne $null) { $word.Quit() | Out-Null }
  if ($doc -ne $null) { [System.Runtime.InteropServices.Marshal]::ReleaseComObject($doc) | Out-Null }
  if ($word -ne $null) { [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null }
  [GC]::Collect()
  [GC]::WaitForPendingFinalizers()
}
