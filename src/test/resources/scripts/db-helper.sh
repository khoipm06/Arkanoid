#!/bin/bash
# Database management script for tests
# Usage: ./dh-helper.sh [backup|restore]

ACTION=$1
DB_PATH="data/arkanoid.db"
BACKUP_PATH="data/arkanoid_backup.db"

if [ -z "$ACTION" ]; then
    echo "[ERROR] Usage: $0 [backup|restore]"
    exit 1
fi

case $ACTION in
    backup)
        if [ -f "$DB_PATH" ]; then
            echo "[BACKUP] Creating database backup..."
            cp "$DB_PATH" "$BACKUP_PATH"
            echo "[OK] Backup created: $BACKUP_PATH"
        else
            echo "[WARNING] Database not found at: $DB_PATH"
            exit 1
        fi
        ;;
    restore)
        if [ -f "$BACKUP_PATH" ]; then
            echo "[RESTORE] Restoring database from backup..."
            cp "$BACKUP_PATH" "$DB_PATH"
            rm "$BACKUP_PATH"
            echo "[OK] Database restored and backup cleaned up"
        else
            echo "[WARNING] Backup not found at: $BACKUP_PATH"
        fi
        ;;
    *)
        echo "[ERROR] Invalid action: $ACTION. Use 'backup' or 'restore'"
        exit 1
        ;;
esac
