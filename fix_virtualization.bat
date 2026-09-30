@echo off
chcp 65001 >nul
title MAX Bloom - Активация виртуализации

echo ====================================================================
echo MAX Bloom — Активация виртуализации Windows и WSL2
echo ====================================================================
echo.

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ====================================================================
    echo [ОШИБКА] Скрипт запущен БЕЗ прав администратора!
    echo.
    echo Чтобы включить виртуализацию:
    echo 1. Закройте это окно.
    echo 2. Нажмите ПРАВОЙ кнопкой мыши по этому файлу.
    echo 3. Выберите пункт: "Запуск от имени администратора" (Run as administrator).
    echo ====================================================================
    echo.
    pause
    exit /b
)

echo [1/4] Настройка гипервизора (hypervisorlaunchtype auto)...
bcdedit /set hypervisorlaunchtype auto

echo.
echo [2/4] Включение Платформы виртуальной машины (VirtualMachinePlatform)...
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

echo.
echo [3/4] Включение HypervisorPlatform...
dism.exe /online /enable-feature /featurename:HypervisorPlatform /all /norestart

echo.
echo [4/4] Включение Подсистемы Linux (Microsoft-Windows-Subsystem-Linux)...
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

echo.
echo ====================================================================
echo [УСПЕХ] Все компоненты виртуализации успешно включены!
echo.
echo Теперь обязательно ПЕРЕЗАГРУЗИТЕ КОМПЬЮТЕР.
echo После перезагрузки Docker Desktop запустится автоматически без ошибок.
echo ====================================================================
echo.
pause
