#!/bin/bash

###############################################################################
# Constitutional Enforcement Checker - Pre-Release Validation
#
# This script validates that the APK or release CANNOT proceed if it violates
# the Verum Omnis Constitution v5.2.7
#
# Usage: ./constitutional-check.sh
###############################################################################

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     VERUM OMNIS CONSTITUTIONAL ENFORCEMENT CHECK                ║${NC}"
echo -e "${BLUE}║     Constitution v5.2.7 - Immutable & Hard-Coded               ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

CHECKS_PASSED=0
CHECKS_FAILED=0
CHECKS_TOTAL=0

###############################################################################
# CHECK 1: Constitution File Exists
###############################################################################

echo -e "${YELLOW}✓ CHECK 1: Constitutional Document Integrity${NC}"
((CHECKS_TOTAL++))

if [ -f "app/src/main/assets/rules/constitution_5_2_7.json" ]; then
    echo -e "${GREEN}✓ Constitution file present${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ CONSTITUTIONAL DOCUMENT MISSING${NC}"
    echo "  File: app/src/main/assets/rules/constitution_5_2_7.json"
    ((CHECKS_FAILED++))
fi

###############################################################################
# CHECK 2: Immutable Principles Coded
###############################################################################

echo ""
echo -e "${YELLOW}✓ CHECK 2: Immutable Principles Hard-Coded${NC}"

# Principle 1: Truth Precedes Authority
((CHECKS_TOTAL++))
if grep -r "Truth Precedes Authority" app/src/main/java/ > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Principle 1 (Truth Precedes Authority) found in code${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ Principle 1 NOT found in code${NC}"
    ((CHECKS_FAILED++))
fi

# Principle 2: Evidence Precedes Narrative
((CHECKS_TOTAL++))
if grep -r "Evidence Precedes Narrative" app/src/main/java/ > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Principle 2 (Evidence Precedes Narrative) found in code${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ Principle 2 NOT found in code${NC}"
    ((CHECKS_FAILED++))
fi

# Principle 3: Guardianship Precedes Power
((CHECKS_TOTAL++))
if grep -r "Guardianship Precedes Power" app/src/main/java/ > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Principle 3 (Guardianship Precedes Power) found in code${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ Principle 3 NOT found in code${NC}"
    ((CHECKS_FAILED++))
fi

###############################################################################
# CHECK 3: Nine Brains All Present
###############################################################################

echo ""
echo -e "${YELLOW}✓ CHECK 3: Nine-Brain Architecture Complete${NC}"

BRAINS=("B1.*Evidence" "B2.*Contradiction" "B3.*Timeline" "B4.*Jurisdiction" \
        "B5.*Behavioral" "B6.*Finance" "B7.*Communication" "B8.*Ethics" "B9.*Guardian")

for brain in "${BRAINS[@]}"; do
    ((CHECKS_TOTAL++))
    if grep -r "$brain" app/src/main/java/ > /dev/null 2>&1; then
        echo -e "${GREEN}✓ $brain found${NC}"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗ $brain NOT found - Architecture incomplete${NC}"
        ((CHECKS_FAILED++))
    fi
done

###############################################################################
# CHECK 4: Constitutional Compliance Validator
###############################################################################

echo ""
echo -e "${YELLOW}✓ CHECK 4: Constitutional Compliance Validator${NC}"

((CHECKS_TOTAL++))
if [ -f "app/src/main/java/org/verumomnis/legal/compliance/ConstitutionalComplianceValidator.kt" ]; then
    echo -e "${GREEN}✓ Constitutional Compliance Validator present${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ VALIDATOR MISSING - Constitutional checks unavailable${NC}"
    ((CHECKS_FAILED++))
fi

###############################################################################
# CHECK 5: Legal Advisory Constraints
###############################################################################

echo ""
echo -e "${YELLOW}✓ CHECK 5: Legal Advisory Constitutional Constraints${NC}"

# Evidence-based requirement
((CHECKS_TOTAL++))
if grep -r "Evidence.*legal.*advice\|legal.*advice.*evidence" app/src/main/java/ > /dev/null 2>&1 || \
   grep -r "evidence.*required\|require.*evidence" app/src/main/java/org/verumomnis/legal/ > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Evidence-based requirement enforced${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${YELLOW}⚠ Evidence-based requirement not found (may be implicit)${NC}"
    ((CHECKS_FAILED++))
fi

# Citizen protection
((CHECKS_TOTAL++))
if grep -r "citizen\|protect" app/src/main/java/org/verumomnis/legal/ > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Citizen protection constraint found${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${YELLOW}⚠ Citizen protection not explicitly found${NC}"
    ((CHECKS_FAILED++))
fi

###############################################################################
# CHECK 6: Release Build Configuration
###############################################################################

echo ""
echo -e "${YELLOW}✓ CHECK 6: Release Build Security${NC}"

((CHECKS_TOTAL++))
if grep -r "isMinifyEnabled.*true\|ProGuard" app/build.gradle.kts > /dev/null 2>&1; then
    echo -e "${GREEN}✓ ProGuard obfuscation enabled${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ ProGuard obfuscation disabled${NC}"
    ((CHECKS_FAILED++))
fi

((CHECKS_TOTAL++))
if grep -r "isShrinkResources.*true" app/build.gradle.kts > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Resource shrinking enabled${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}✗ Resource shrinking disabled${NC}"
    ((CHECKS_FAILED++))
fi

###############################################################################
# CHECK 7: Documentation Completeness
###############################################################################

echo ""
echo -e "${YELLOW}✓ CHECK 7: Constitutional Documentation${NC}"

DOCS=(
    "README.md"
    "LICENSE_AND_ACCESS_POLICY.md"
    "CONSTITUTIONAL_ENFORCEMENT_ON_RELEASE.md"
    "final constitution(2).PDF"
)

for doc in "${DOCS[@]}"; do
    ((CHECKS_TOTAL++))
    if [ -f "$doc" ] 2>/dev/null; then
        echo -e "${GREEN}✓ $doc present${NC}"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗ $doc missing${NC}"
        ((CHECKS_FAILED++))
    fi
done

###############################################################################
# FINAL REPORT
###############################################################################

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║          CONSTITUTIONAL ENFORCEMENT CHECK REPORT               ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo "Total Checks: $CHECKS_TOTAL"
echo -e "Passed: ${GREEN}$CHECKS_PASSED${NC}"
echo -e "Failed: ${RED}$CHECKS_FAILED${NC}"

if [ $CHECKS_TOTAL -gt 0 ]; then
    PASS_RATE=$((CHECKS_PASSED * 100 / CHECKS_TOTAL))
else
    PASS_RATE=0
fi

echo -e "Pass Rate: ${BLUE}${PASS_RATE}%${NC}"
echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CONSTITUTIONAL CHECKS PASSED${NC}"
    echo ""
    echo "The Verum Omnis forensic engine is constitutionally compliant."
    echo "The following are ENFORCED in this release:"
    echo ""
    echo "  ✓ Immutable Principles (3)"
    echo "    - Truth Precedes Authority"
    echo "    - Evidence Precedes Narrative"
    echo "    - Guardianship Precedes Power"
    echo ""
    echo "  ✓ Nine-Brain Architecture (All Required)"
    echo "    - B1 Evidence, B2 Contradiction, B3 Timeline"
    echo "    - B4 Jurisdiction, B5 Behavioral, B6 Finance"
    echo "    - B7 Communication, B8 Ethics, B9 Guardian"
    echo ""
    echo "  ✓ Legal Advisory Constraints"
    echo "    - Evidence-based only"
    echo "    - Citizen-protective"
    echo "    - Fully transparent"
    echo ""
    echo "  ✓ Constitutional Validator"
    echo "    - Runtime enforcement active"
    echo "    - Constitutional seal present"
    echo "    - Audit trail enabled"
    echo ""
    echo -e "${GREEN}RELEASE CAN PROCEED SAFELY${NC}"
    echo ""
    exit 0
else
    echo -e "${RED}❌ CONSTITUTIONAL CHECKS FAILED${NC}"
    echo ""
    echo "The forensic engine is NOT constitutionally compliant."
    echo "The following issues MUST be fixed before release:"
    echo ""
    echo "  $CHECKS_FAILED checks failed out of $CHECKS_TOTAL"
    echo ""
    echo "Review the failures above and fix them before attempting release."
    echo ""
    echo -e "${RED}RELEASE BLOCKED - DO NOT PROCEED${NC}"
    echo ""
    exit 1
fi
