Write-Host "Iniciando limpeza de resources antigos..." -ForegroundColor Cyan

$folders = @(
    "midgard-modules/midgard-character/src/main/resources",
    "midgard-modules/midgard-classes/src/main/resources",
    "midgard-modules/midgard-combat/src/main/resources",
    "midgard-modules/midgard-essentials/src/main/resources",
    "midgard-modules/midgard-item/src/main/resources",
    "midgard-modules/midgard-mythicmobs/src/main/resources",
    "midgard-modules/midgard-performance/src/main/resources",
    "midgard-modules/midgard-security/src/main/resources",
    "midgard-modules/midgard-spells/src/main/resources",
    "midgard-modules/midgard-territory/src/main/resources",
    "midgard-core/src/main/resources"
)

foreach ($folder in $folders) {
    if (Test-Path $folder) {
        Write-Host "Removendo: $folder"
        Remove-Item -Path $folder -Recurse -Force
    } else {
        Write-Host "Pasta não encontrada (já removida?): $folder" -ForegroundColor DarkGray
    }
}

Write-Host "Limpeza concluída! Apenas midgard-loader/src/main/resources foi mantido." -ForegroundColor Green
Pause
