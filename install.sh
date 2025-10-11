#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BIN_DIR="${HOME}/.local/bin"
APP_DIR="${HOME}/.local/kinfra"
APP_NAME="kinfra"
GITHUB_REPO="kigawa-net/kinfra"
VERSION="${1:-latest}"

echo -e "${GREEN}Installing ${APP_NAME}...${NC}"

# Create directories if they don't exist
mkdir -p "${BIN_DIR}"
mkdir -p "${APP_DIR}"

# Determine the latest version if not specified
if [ "${VERSION}" = "latest" ]; then
    echo "Fetching latest release version..."
    VERSION=$(curl -s "https://api.github.com/repos/${GITHUB_REPO}/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
    if [ -z "${VERSION}" ]; then
        echo -e "${RED}Failed to fetch latest version${NC}"
        exit 1
    fi
    echo -e "${GREEN}Latest version: ${VERSION}${NC}"
fi

# Construct download URL
DOWNLOAD_URL="https://github.com/${GITHUB_REPO}/releases/download/${VERSION}/kinfra-cli-${VERSION#v}.jar"

echo "Downloading ${APP_NAME} from ${DOWNLOAD_URL}..."

# Download the JAR file to app directory
JAR_PATH="${APP_DIR}/${APP_NAME}.jar"
if curl -L -f -o "${JAR_PATH}" "${DOWNLOAD_URL}"; then
    echo -e "${GREEN}Downloaded successfully${NC}"
else
    echo -e "${RED}Failed to download ${APP_NAME}${NC}"
    echo -e "${YELLOW}URL attempted: ${DOWNLOAD_URL}${NC}"
    exit 1
fi

# Create wrapper script in bin directory
WRAPPER_PATH="${BIN_DIR}/${APP_NAME}"
cat > "${WRAPPER_PATH}" << 'EOF'
#!/bin/bash
exec java -jar "${HOME}/.local/kinfra/kinfra.jar" "$@"
EOF

# Make wrapper executable
chmod +x "${WRAPPER_PATH}"

echo -e "${GREEN}${APP_NAME} has been installed${NC}"
echo "  JAR: ${JAR_PATH}"
echo "  Wrapper: ${WRAPPER_PATH}"

# Check if bin directory is in PATH
if [[ ":$PATH:" != *":${BIN_DIR}:"* ]]; then
    echo -e "${YELLOW}${BIN_DIR} is not in your PATH${NC}"
    echo "Adding to PATH configuration..."

    # Detect shell configuration file
    SHELL_CONFIG=""
    if [ -n "$ZSH_VERSION" ] || [ -f "${HOME}/.zshrc" ]; then
        SHELL_CONFIG="${HOME}/.zshrc"
    elif [ -n "$BASH_VERSION" ] || [ -f "${HOME}/.bashrc" ]; then
        SHELL_CONFIG="${HOME}/.bashrc"
    elif [ -f "${HOME}/.bash_profile" ]; then
        SHELL_CONFIG="${HOME}/.bash_profile"
    elif [ -f "${HOME}/.profile" ]; then
        SHELL_CONFIG="${HOME}/.profile"
    fi

    if [ -n "$SHELL_CONFIG" ]; then
        # Check if PATH export already exists in the file
        if ! grep -q "export PATH=\"\${HOME}/.local/bin:\${PATH}\"" "$SHELL_CONFIG" 2>/dev/null; then
            echo "" >> "$SHELL_CONFIG"
            echo "# Added by kinfra installer" >> "$SHELL_CONFIG"
            echo "export PATH=\"\${HOME}/.local/bin:\${PATH}\"" >> "$SHELL_CONFIG"
            echo -e "${GREEN}Added PATH to ${SHELL_CONFIG}${NC}"
            echo -e "${YELLOW}Please run: source ${SHELL_CONFIG}${NC}"
            echo "Or restart your terminal to apply changes"
        else
            echo -e "${GREEN}PATH already configured in ${SHELL_CONFIG}${NC}"
        fi
    else
        echo -e "${YELLOW}Could not detect shell configuration file${NC}"
        echo "Please add the following line to your shell configuration file:"
        echo "  export PATH=\"\${HOME}/.local/bin:\${PATH}\""
    fi
else
    echo -e "${GREEN}${BIN_DIR} is already in your PATH${NC}"
fi

echo ""
echo -e "${GREEN}Installation complete!${NC}"
echo "Run '${APP_NAME} --help' to get started"