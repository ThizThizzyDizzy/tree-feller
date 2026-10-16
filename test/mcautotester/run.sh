#!/usr/bin/env bash
set -euo pipefail

project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)
local_config="$project_dir/test/mcautotester/local.env"
if [[ -f "$local_config" ]]; then
    source "$local_config"
fi
runner_home=${MCAUTOTESTER_HOME:-"$project_dir/../MCAutoTester/dist"}
runner_home=$(cd -- "$runner_home" && pwd)
if (($# == 0)); then
    set -- treefeller-option-messages-chat treefeller-option-messages-actionbar \
        treefeller-option-debug-text-level \
        treefeller-option-debug-overlay-level \
        treefeller-option-debug-step-through \
        treefeller-option-item-custom-model-data \
        treefeller-option-item-material \
        treefeller-option-item-durability \
        treefeller-option-item-custom-name \
        treefeller-option-item-lore \
        treefeller-option-item-attributes \
        treefeller-option-item-enchantments \
        treefeller-option-result-effects \
        treefeller-option-result-effects-chance \
        treefeller-option-result-effects-location \
        treefeller-option-result-effects-type \
        treefeller-option-result-effects-filter \
        treefeller-option-result-effects-particle \
        treefeller-option-result-effects-x \
        treefeller-option-result-effects-y \
        treefeller-option-result-effects-z \
        treefeller-option-result-effects-dx \
        treefeller-option-result-effects-dy \
        treefeller-option-result-effects-dz \
        treefeller-option-result-effects-speed \
        treefeller-option-result-effects-count \
        treefeller-option-result-effects-r \
        treefeller-option-result-effects-g \
        treefeller-option-result-effects-b \
        treefeller-option-result-effects-size \
        treefeller-option-result-effects-item \
        treefeller-option-result-effects-block \
        treefeller-option-result-effects-sound \
        treefeller-option-result-effects-volume \
        treefeller-option-result-effects-pitch \
        treefeller-option-result-effects-power \
        treefeller-option-result-effects-fire \
        treefeller-option-result-effects-permanent \
        treefeller-option-result-effects-tags \
        treefeller-option-result-cascade-enable \
        treefeller-option-result-cascade-parallel-cascade-limit \
        treefeller-option-result-cascade-cascade-check-limit \
        treefeller-option-trees-cascade-trees \
        treefeller-option-trigger-default-enabled \
        treefeller-option-trigger-permissions \
        treefeller-option-detection-root-distance \
        treefeller-option-detection-disconnected-trunk-distance \
        treefeller-option-detection-max-leaf-distance-from-top \
        treefeller-option-detection-leaf-detect-range \
        treefeller-option-detection-diagonal-leaves \
        treefeller-option-detection-secondary-tree-verification \
        treefeller-option-detection-block-data-rules-ignore-parallel-trunk-pillars \
        treefeller-option-detection-block-data-rules-use-leaf-distance \
        treefeller-option-detection-decorations-blocks \
        treefeller-option-detection-decorations-direction \
        treefeller-option-detection-decorations-column \
        treefeller-option-criteria-max-height \
        treefeller-option-criteria-required-trunk-min treefeller-option-criteria-required-trunk-max \
        treefeller-option-criteria-required-leaves-min treefeller-option-criteria-required-leaves-max \
        treefeller-option-criteria-require-cross-section \
        treefeller-option-criteria-trunk-filter-required treefeller-option-criteria-trunk-filter-blacklist \
        treefeller-option-criteria-leaf-filter-required treefeller-option-criteria-leaf-filter-blacklist \
        treefeller-option-criteria-decoration-filter-required treefeller-option-criteria-decoration-filter-blacklist \
        treefeller-option-criteria-trunk-max-horizontal-line \
        treefeller-option-criteria-height-ratio-min treefeller-option-criteria-height-ratio-max \
        treefeller-option-criteria-trunk-vertical-ratio-min treefeller-option-criteria-trunk-vertical-ratio-max \
        treefeller-option-trigger-cooldown \
        treefeller-option-trigger-food treefeller-option-trigger-health treefeller-option-trigger-saturation \
        treefeller-option-trigger-creative-mode treefeller-option-trigger-survival-mode \
        treefeller-option-trigger-adventure-mode treefeller-option-trigger-with-sneaking \
        treefeller-option-trigger-without-sneaking \
        treefeller-option-trigger-day-time treefeller-option-trigger-moon-phase \
        treefeller-option-trigger-biomes treefeller-option-trigger-dimensions \
        treefeller-option-cutting-leave-stump \
        treefeller-option-cutting-leaf-break-range \
        treefeller-option-cutting-partial-trunk-limit \
        treefeller-option-cutting-trunk-damage-mult \
        treefeller-option-cutting-leaves-damage-mult \
        treefeller-option-cutting-decorations-damage-mult \
        treefeller-option-cutting-respect-unbreaking \
        treefeller-option-cutting-respect-unbreakable \
        treefeller-option-cutting-stacked-tools \
        treefeller-option-cutting-prevent-breakage \
        treefeller-option-cutting-allow-partial \
        treefeller-option-cutting-allow-partial-tool \
        treefeller-option-cutting-cutting-animation \
        treefeller-option-cutting-animation-delay \
        treefeller-option-result-trunk-fortune treefeller-option-result-leaves-fortune \
        treefeller-option-result-decoration-fortune treefeller-option-result-trunk-silk-touch \
        treefeller-option-result-leaves-silk-touch treefeller-option-result-decoration-silk-touch \
        treefeller-option-result-trunk-drop-chance treefeller-option-result-leaves-drop-chance \
        treefeller-option-result-decorations-drop-chance treefeller-option-result-drop-conversions \
        treefeller-option-result-drop-to-inventory \
        treefeller-option-result-sapling-replant-enable \
        treefeller-option-result-sapling-replant-use-tree-saplings \
        treefeller-option-result-sapling-replant-use-inventory-saplings \
        treefeller-option-result-sapling-replant-spawn-saplings \
        treefeller-option-result-sapling-replant-sapling-timeout \
        treefeller-option-result-sapling-replant-grasses \
        treefeller-option-trees-saplings treefeller-option-trees-max-saplings \
        treefeller-option-result-consumed-food-base treefeller-option-result-consumed-food-trunk \
        treefeller-option-result-consumed-food-leaves treefeller-option-result-consumed-food-decoration \
        treefeller-option-result-consumed-health-base treefeller-option-result-consumed-health-trunk \
        treefeller-option-result-consumed-health-leaves treefeller-option-result-consumed-health-decoration \
        treefeller-option-breaking-trunk-behavior treefeller-option-breaking-leaves-behavior \
        treefeller-option-breaking-decoration-behavior \
        treefeller-option-breaking-fall-direction treefeller-option-breaking-fall-direction-yaw \
        treefeller-option-breaking-fall-cardinal \
        treefeller-option-breaking-directional-fall-velocity treefeller-option-breaking-vertical-fall-velocity \
        treefeller-option-breaking-explosive-fall-velocity treefeller-option-breaking-random-fall-velocity \
        treefeller-option-breaking-trunk-behavior-fall-damage-amount treefeller-option-breaking-trunk-behavior-fall-damage-maximum \
        treefeller-option-breaking-leaves-behavior-fall-damage-amount treefeller-option-breaking-leaves-behavior-fall-damage-maximum \
        treefeller-option-breaking-decoration-behavior-fall-damage-amount treefeller-option-breaking-decoration-behavior-fall-damage-maximum \
        treefeller-option-breaking-fall-delay treefeller-option-breaking-block-conversions \
        treefeller-option-breaking-landing treefeller-option-breaking-landing-in-place treefeller-option-result-rotate-logs treefeller-option-result-overrideable-blocks
fi

ant -q -f "$project_dir/Builder/build.xml" -Dmkdist.disabled=true jar
run_dir=$(mktemp -d "${TMPDIR:-/tmp}/treefeller-mcautotester.XXXXXXXX")
printf 'Run output and recordings: %s\n' "$run_dir"
cd -- "$run_dir"

{
    printf 'test'
    if [[ -n "${MCAUTOTESTER_HOST:-}" ]]; then
        printf ' -h "%s"' "$MCAUTOTESTER_HOST"
    fi
    printf ' -s "%s" -p "%s" -c' \
        "${MCAUTOTESTER_TARGET:-paper:1.21.5}" \
        "$project_dir/Builder/dist/treefeller-DEV.jar"
    printf ' -t "%s"' "$@"
    printf '\nexit\n'
} | java -Ddizzyengine.headless=true -jar "$runner_home/MCAutoTester.jar" 2>&1 | tee run.log
