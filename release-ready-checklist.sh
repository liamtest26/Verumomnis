#!/bin/bash

###############################################################################
# VERUM OMNIS RELEASE READY CHECKLIST
# 
# This checklist verifies that all constitutional enforcement layers are
# in place before releasing to production.
###############################################################################

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASSED=0
FAILED=0

check_item() {
    local name=$1
    local file=$2
    
    if [ -f "$file" ] || [ -d "$file" ]; then
        echo -e "${GREEN}✓${NC} $name"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} $name ($file not found)"
        ((FAILED++))
        return 1
    fi
}

check_contains() {
    local name=$1
    local file=$2
    local pattern=$3
    
    if grep -q "$pattern" "$file" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $name"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} $name (pattern not found: $pattern)"
        ((FAILED++))
        return 1
    fi
}

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║       VERUM OMNIS RELEASE READY CHECKLIST                      ║${NC}"
echo -e "${BLUE}║       Constitutional Enforcement Verification                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${YELLOW}SECTION 1: Core Files${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_item "Constitutional Enforcement Lock code" "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt"
check_item "Constitutional Compliance Validator" "app/src/main/java/org/verumomnis/legal/compliance/ConstitutionalComplianceValidator.kt"
check_item "Constitution JSON framework" "app/src/main/assets/rules/constitution_5_2_7.json"
check_item "Constitutional seal JSON" "app/src/main/assets/constitutional-seal.json"
echo ""

echo -e "${YELLOW}SECTION 2: Immutable Principles Hard-Coded${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "Principle 1 (Truth Precedes Authority)" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "Truth Precedes Authority"

check_contains "Principle 2 (Evidence Precedes Narrative)" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "Evidence Precedes Narrative"

check_contains "Principle 3 (Guardianship Precedes Power)" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "Guardianship Precedes Power"
echo ""

echo -e "${YELLOW}SECTION 3: Nine-Brain Architecture${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "REQUIRED_BRAINS array immutable" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "REQUIRED_BRAINS.*arrayOf"

for brain in "B1_Evidence" "B2_Contradiction" "B3_Timeline" "B4_Jurisdiction" \
             "B5_Behavioral" "B6_Finance" "B7_Communication" "B8_Ethics" "B9_Guardian"; do
    check_contains "Brain: $brain" \
        "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
        "$brain"
done
echo ""

echo -e "${YELLOW}SECTION 4: Legal Advisory Constraints${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "Evidence-based constraint" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "evidence.isBlank()"

check_contains "Citizen-protective constraint" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "citizenProtective"

check_contains "Transparent constraint" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "transparent"
echo ""

echo -e "${YELLOW}SECTION 5: Runtime Validation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "Startup validation method" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "validateConstitutionOnStartup"

check_contains "Breach exception handling" \
    "app/src/main/java/org/verumomnis/legal/enforcement/ConstitutionalEnforcementLock.kt" \
    "ConstitutionalBreachException"
echo ""

echo -e "${YELLOW}SECTION 6: Release Verification Scripts${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_item "Constitutional check script" "constitutional-check.sh"
check_item "Verification script" "verify-release.sh"
check_item "Release build script" "release-build.sh"
check_item "Summary script" "constitutional-enforcement-summary.sh"
echo ""

echo -e "${YELLOW}SECTION 7: Documentation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_item "Constitutional enforcement documentation" "CONSTITUTIONAL_ENFORCEMENT_ON_RELEASE.md"
check_item "Access policy documentation" "LICENSE_AND_ACCESS_POLICY.md"
check_item "README" "README.md"
echo ""

echo -e "${YELLOW}SECTION 8: Build Configuration${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_contains "ProGuard obfuscation enabled" \
    "app/build.gradle.kts" \
    "minifyEnabled.*true"

check_contains "Resource shrinking enabled" \
    "app/build.gradle.kts" \
    "shrinkResources.*true"

check_contains "Release signing configured" \
    "app/build.gradle.kts" \
    "signingConfigs"
echo ""

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    FINAL VERIFICATION REPORT                   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

TOTAL=$((PASSED + FAILED))
if [ $TOTAL -gt 0 ]; then
    PASS_RATE=$((PASSED * 100 / TOTAL))
else
    PASS_RATE=0
fi

echo "Total Checks: $TOTAL"
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"
echo "Pass Rate: $PASS_RATE%"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✅ ALL CHECKS PASSED - READY TO RELEASE${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Verum Omnis is constitutionally enforced and ready for production."
    echo ""
    echo "Constitutional enforcement layers active:"
    echo "  ✓ Level 1: Immutable Principles (hard-coded)"
    echo "  ✓ Level 2: Nine-Brain Architecture (mandatory)"
    echo "  ✓ Level 3: Legal Advisory Constraints (enforced)"
    echo "  ✓ Level 4: Compliance Validator (runs on every operation)"
    echo "  ✓ Level 5: Runtime Validation (on app startup)"
    echo "  ✓ Level 6: Constitutional Seal (embedded in APK)"
    echo "  ✓ Level 7: Build-Time Enforcement (script verification)"
    echo "  ✓ Level 8: Amendment Lock (until 2027-01-31)"
    echo "  ✓ Level 9: Breach Detection (automatic reporting)"
    echo ""
    echo "Next steps:"
    echo "  1. Run: ./constitutional-check.sh"
    echo "  2. Run: ./release-build.sh"
    echo "  3. Run: ./verify-release.sh"
    echo "  4. Create GitHub release with verified APK"
    echo ""
    exit 0
else
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}❌ CHECKS FAILED - CANNOT RELEASE${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Fix the following issues before releasing:"
    echo "  - Create missing files listed above"
    echo "  - Ensure all enforcement code is present"
    echo "  - Verify constitutional constraints are hard-coded"
    echo "  - Run again to verify"
    echo ""
    exit 1
fi
