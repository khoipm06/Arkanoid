# Database management script for tests
# Usage: ./dh-helper.ps1 [backup|restore]

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("backup", "restore")]
    [string]$Action
)

$DB_PATH = "data/arkanoid.db"
$BACKUP_PATH = "data/arkanoid_backup.db"

switch ($Action) {
    "backup" {
        if (Test-Path $DB_PATH) {
            Write-Host "[BACKUP] Creating database backup..." -ForegroundColor Cyan
            Copy-Item $DB_PATH $BACKUP_PATH -Force
            Write-Host "[OK] Backup created: $BACKUP_PATH" -ForegroundColor Green
        } else {
            Write-Host "[WARNING] Database not found at: $DB_PATH" -ForegroundColor Yellow
            exit 1
        }
    }
    "restore" {
        if (Test-Path $BACKUP_PATH) {
            Write-Host "[RESTORE] Restoring database from backup..." -ForegroundColor Cyan
            Copy-Item $BACKUP_PATH $DB_PATH -Force
            Remove-Item $BACKUP_PATH
            Write-Host "[OK] Database restored and backup cleaned up" -ForegroundColor Green
        } else {
            Write-Host "[WARNING] Backup not found at: $BACKUP_PATH" -ForegroundColor Yellow
        }
    }
}
