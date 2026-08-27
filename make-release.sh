#!/usr/bin/env bash
# make-release.sh – Prepare a Java project release
# Usage: ./make-release.sh [--no-tag] [--no-tar] [--skip-tests] [--skip-clean] [--bump X.Y.Z]

set -e

# ---------- Colors ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ---------- Detect OS for sed compatibility ----------
if [[ "$OSTYPE" == "darwin"* ]]; then
    SED_INLINE="sed -i ''"
else
    SED_INLINE="sed -i"
fi

# ---------- Project specific settings ----------
# Derive project name from current directory name, or set a default
PROJECT_NAME="${PROJECT_NAME:-$(basename "$PWD")}"
# Version file (can be overridden by environment)
VERSION_FILE="${VERSION_FILE:-VERSION}"
# Maven/Gradle detection
HAS_MAVEN=false
HAS_GRADLE=false
if command -v mvn &> /dev/null && [ -f "pom.xml" ]; then
    HAS_MAVEN=true
fi
if command -v gradle &> /dev/null && [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
    HAS_GRADLE=true
fi

# ---------- Functions ----------
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Options:
  --no-tag       Do not create the Git tag (only the tarball)
  --no-tar       Do not create the tarball (only the tag)
  --skip-tests   Skip running tests before release
  --skip-clean   Skip cleaning build artifacts before release
  --bump VERSION Update the version in VERSION file to VERSION (e.g. --bump 1.2.3)
                   and also update pom.xml/build.gradle if present.
  --help         Show this help

Description:
  This script prepares a Java project release:
    - Optionally bumps the version in VERSION, pom.xml (Maven) or build.gradle (Gradle)
    - Checks that the Git repository is clean (or offers to commit changes)
    - Extracts the version from VERSION file
    - Optionally runs tests (using Maven/Gradle if available, or tools/run-tests.sh)
    - Optionally cleans build artifacts (using Maven/Gradle if available, or tools/clean-build.sh)
    - Creates a tarball of the source code (excluding build outputs)
    - Creates a Git tag (optional)
    - Displays instructions for pushing and publishing

The version is read from VERSION file (single line with X.Y.Z format).
EOF
    exit 0
}

bump_version() {
    local new_version="$1"
    if [ -z "$new_version" ]; then
        echo -e "${RED}Error: specify new version (e.g., 1.2.3)${NC}" >&2
        exit 1
    fi
    # Update VERSION file
    echo "$new_version" > "$VERSION_FILE"
    echo -e "${GREEN}Version set to $new_version in $VERSION_FILE${NC}"

    # Update Maven pom.xml if present
    if [ -f "pom.xml" ]; then
        echo -e "${BLUE}Updating version in pom.xml...${NC}"
        mvn versions:set -DnewVersion="$new_version" -DgenerateBackupPoms=false > /dev/null 2>&1 || \
            echo -e "${YELLOW}Could not update pom.xml automatically (mvn versions:set failed). Please update manually.${NC}"
    fi

    # Update Gradle build.gradle if present
    if [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
        local gradle_file="build.gradle"
        [ -f "build.gradle.kts" ] && gradle_file="build.gradle.kts"
        echo -e "${BLUE}Updating version in $gradle_file...${NC}"
        # Simple sed replacement for version line (works for common patterns)
        # This is a simplistic approach; you might want to use a gradle plugin.
        if grep -q "^version\s*=" "$gradle_file"; then
            $SED_INLINE "s/^version\s*=\s*'.*'/version = '$new_version'/" "$gradle_file"
            $SED_INLINE "s/^version\s*=\s*\".*\"/version = \"$new_version\"/" "$gradle_file"
            echo -e "${GREEN}Version updated in $gradle_file${NC}"
        else
            echo -e "${YELLOW}Could not find 'version =' pattern in $gradle_file. Please update manually.${NC}"
        fi
    fi

    # Update documentation (if docs/index.md exists)
    if [ -f docs/index.md ]; then
        sed -i.bak "s/\*\*Version:\*\* .*/\*\*Version:\*\* $new_version  /" docs/index.md && rm -f docs/index.md.bak
        echo -e "${GREEN}Version updated in docs/index.md${NC}"
    fi
}

# ---------- Parse arguments ----------
CREATE_TAG=true
CREATE_TAR=true
RUN_TESTS=true
RUN_CLEAN=true
BUMP_VERSION=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --no-tag)      CREATE_TAG=false ;;
        --no-tar)      CREATE_TAR=false ;;
        --skip-tests)  RUN_TESTS=false ;;
        --skip-clean)  RUN_CLEAN=false ;;
        --bump)        BUMP_VERSION="$2"; shift ;;
        --help)        show_help ;;
        *)             echo -e "${RED}Unknown option: $1${NC}"; show_help ;;
    esac
    shift
done

# ---------- Checks ----------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}Error: this directory is not a Git repository.${NC}"
    exit 1
fi

# ---------- Bump version if requested ----------
if [ -n "$BUMP_VERSION" ]; then
    echo -e "${BLUE}Bumping version to ${BUMP_VERSION}...${NC}"
    bump_version "$BUMP_VERSION"
    # Check if any files changed; if so, commit them
    if ! git diff --quiet "$VERSION_FILE" docs/index.md pom.xml build.gradle build.gradle.kts 2>/dev/null; then
        echo -e "${YELLOW}Version files have been modified. Committing the version bump...${NC}"
        git add "$VERSION_FILE" docs/index.md pom.xml build.gradle build.gradle.kts 2>/dev/null || true
        git commit -m "Bump version to ${BUMP_VERSION}"
        echo -e "${GREEN}Version bump committed.${NC}"
    fi
fi

# Check for uncommitted changes
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo -e "${YELLOW}Warning: there are uncommitted changes.${NC}"
    read -p "Do you want to commit them now? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Committing...${NC}"
        git add -A
        git commit -m "WIP before release"
    else
        echo -e "${RED}Aborting: changes must be committed.${NC}"
        exit 1
    fi
fi

# ---------- Run tests ----------
if [ "$RUN_TESTS" = true ]; then
    if [ -f "./tools/run-tests.sh" ]; then
        echo -e "${BLUE}Running tests via tools/run-tests.sh...${NC}"
        if ! bash ./tools/run-tests.sh; then
            echo -e "${RED}Tests failed. Aborting release.${NC}"
            exit 1
        fi
    elif [ "$HAS_MAVEN" = true ]; then
        echo -e "${BLUE}Running Maven tests...${NC}"
        if ! mvn test; then
            echo -e "${RED}Tests failed. Aborting release.${NC}"
            exit 1
        fi
    elif [ "$HAS_GRADLE" = true ]; then
        echo -e "${BLUE}Running Gradle tests...${NC}"
        if ! gradle test; then
            echo -e "${RED}Tests failed. Aborting release.${NC}"
            exit 1
        fi
    else
        echo -e "${YELLOW}No test runner found. Skipping tests.${NC}"
    fi
    echo -e "${GREEN}Tests passed.${NC}"
fi

# ---------- Clean build artifacts ----------
if [ "$RUN_CLEAN" = true ]; then
    if [ -f "./tools/clean-build.sh" ]; then
        echo -e "${BLUE}Cleaning via tools/clean-build.sh...${NC}"
        bash ./tools/clean-build.sh
    elif [ "$HAS_MAVEN" = true ]; then
        echo -e "${BLUE}Cleaning Maven build artifacts...${NC}"
        mvn clean
    elif [ "$HAS_GRADLE" = true ]; then
        echo -e "${BLUE}Cleaning Gradle build artifacts...${NC}"
        gradle clean
    else
        echo -e "${YELLOW}No clean tool found. Skipping clean.${NC}"
    fi
    echo -e "${GREEN}Cleanup done.${NC}"
fi

# ---------- Extract version from VERSION file ----------
if [ ! -f "$VERSION_FILE" ]; then
    echo -e "${RED}Error: $VERSION_FILE not found.${NC}"
    exit 1
fi

VERSION=$(cat "$VERSION_FILE")
if [ -z "$VERSION" ]; then
    echo -e "${RED}Error: VERSION file is empty.${NC}"
    exit 1
fi

SPLIT_CREATED=false

echo -e "${GREEN}Detected version: ${VERSION}${NC}"

# ---------- Check if tag already exists ----------
if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    echo -e "${YELLOW}Tag v$VERSION already exists.${NC}"
    read -p "Do you want to delete it and recreate it? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git tag -d "v$VERSION"
        echo -e "${GREEN}Tag deleted.${NC}"
    else
        echo -e "${RED}Aborting.${NC}"
        exit 1
    fi
fi

# ---------- Summary and confirmation ----------
echo -e "${BLUE}Preparing release v${VERSION} for project $PROJECT_NAME${NC}"
echo "  - Create tag ?     : $([ "$CREATE_TAG" = true ] && echo "YES" || echo "NO")"
echo "  - Create tarball ? : $([ "$CREATE_TAR" = true ] && echo "YES" || echo "NO")"
echo

read -p "Continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}Aborting.${NC}"
    exit 1
fi

# ---------- Create tarball ----------
if [ "$CREATE_TAR" = true ]; then
    TAR_NAME="${PROJECT_NAME}-${VERSION}.tar.gz"
    EXCLUDE=(
        --exclude='.git'
        --exclude="$TAR_NAME"
        --exclude="./$TAR_NAME"
        --exclude='target/'
        --exclude='build/'
        --exclude='out/'
        --exclude='*.class'
        --exclude='*.jar'
        --exclude='*.war'
        --exclude='*.ear'
        --exclude='*.log'
        --exclude='.idea/'
        --exclude='*.iml'
        --exclude='.vscode/'
        --exclude='.settings/'
        --exclude='.project'
        --exclude='.classpath'
        --exclude='.gradle/'
        --exclude='gradle/'
        --exclude='gradlew'
        --exclude='gradlew.bat'
        --exclude='.DS_Store'
        --exclude='*.tmp'
        --exclude='*.swp'
        --exclude='__pycache__'   # just in case
        --exclude='*.pyc'
    )
    echo -e "${BLUE}Creating tarball $TAR_NAME...${NC}"
    tar -czf "$TAR_NAME" "${EXCLUDE[@]}" .
    echo -e "${GREEN}Tarball created: $TAR_NAME${NC}"
fi

# ---------- Split tarball if it exceeds 2GB (GitHub's file size limit) ----------
if [ "$CREATE_TAR" = true ]; then
    TAR_SIZE=$(stat -f%z "$TAR_NAME" 2>/dev/null || stat -c%s "$TAR_NAME" 2>/dev/null)
    SIZE_LIMIT=$((2 * 1024 * 1024 * 1024))  # 2GB in bytes

    if [ "$TAR_SIZE" -gt "$SIZE_LIMIT" ]; then
        echo -e "${YELLOW}Tarball exceeds 2GB GitHub limit ($((TAR_SIZE / 1024 / 1024 / 1024))GB). Splitting...${NC}"
        split -b 1G "$TAR_NAME" "$TAR_NAME.part"
        echo -e "${GREEN}Split into parts:${NC}"
        ls -lh "$TAR_NAME".part*
        # Remove the original if split was successful
        rm "$TAR_NAME"
        echo -e "${GREEN}Original tarball removed. Use .part* files for release.${NC}"
        SPLIT_CREATED=true
    fi
fi

# ---------- Create tag ----------
if [ "$CREATE_TAG" = true ]; then
    echo -e "${BLUE}Creating tag v$VERSION...${NC}"
    git tag -a "v$VERSION" -m "Release v$VERSION"
    echo -e "${GREEN}Tag created.${NC}"
fi

# ---------- Instructions ----------
echo
echo -e "${GREEN}=== Release v$VERSION prepared successfully! ===${NC}"
echo
echo "Next steps:"
if [ "$CREATE_TAG" = true ]; then
    echo "  1. Push the tag:"
    echo "     git push origin v$VERSION"
fi
if [ "$CREATE_TAR" = true ]; then
    if [ "$SPLIT_CREATED" = true ]; then
        echo "  2. Publish the split tarballs:"
        echo "     gh release create v$VERSION ${PROJECT_NAME}-${VERSION}.tar.gz.part* --title \"${PROJECT_NAME} v$VERSION\" --notes \"Release notes...\""
        echo
        echo "     To reconstruct the archive:"
        echo "     cat ${PROJECT_NAME}-${VERSION}.tar.gz.part* > ${PROJECT_NAME}-${VERSION}.tar.gz"
    else
        echo "  2. Publish the tarball:"
        echo "     gh release create v$VERSION ${PROJECT_NAME}-${VERSION}.tar.gz --title \"${PROJECT_NAME} v$VERSION\" --notes \"Release notes...\""
    fi
    echo "  3. (Optional) Verify the tarball content:"
    if [ "$SPLIT_CREATED" = true ]; then
        echo "     cat ${PROJECT_NAME}-${VERSION}.tar.gz.part* | tar -tzf - | head -20"
    else
        echo "     tar -tzf ${PROJECT_NAME}-${VERSION}.tar.gz | head -20"
    fi
fi
echo
echo -e "${BLUE}Happy releasing!${NC}"