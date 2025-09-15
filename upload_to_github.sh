#!/data/data/com.termux/files/usr/bin/bash
# ==========================================
# Script automático para subir cualquier proyecto a GitHub desde Termux
# Autor: ChatGPT + KriztPaw 😎
# ==========================================

echo ""
echo "🚀 Iniciando asistente automático para subir tu proyecto a GitHub"
echo ""

# Verificar si Git está instalado
if ! command -v git &> /dev/null; then
    echo "⚠️ Git no está instalado. Instalando Git..."
    pkg update -y && pkg install git -y
fi

# Configurar Git si es la primera vez
read -p "👤 Ingresa tu nombre de usuario de GitHub: " GIT_NAME
git config --global user.name "$GIT_NAME"

read -p "📧 Ingresa tu correo de GitHub: " GIT_EMAIL
git config --global user.email "$GIT_EMAIL"

echo "✅ Configuración de Git completada."

# Validar si hay archivos en la carpeta actual
if [ -z "$(ls -A .)" ]; then
    echo "⚠️ La carpeta está vacía. Coloca aquí tu proyecto antes de ejecutar este script."
    exit 1
fi

# Solucionar problemas de permisos en /sdcard
PROJECT_DIR=$(pwd)
git config --global --add safe.directory "$PROJECT_DIR"

# Inicializar repositorio si no existe
if [ ! -d ".git" ]; then
    echo "🟢 Inicializando repositorio Git..."
    git init
else
    echo "ℹ️ Repositorio Git ya existente."
fi

# Preguntar por la URL del repositorio
read -p "🌐 Ingresa la URL de tu repositorio en GitHub: " GITHUB_URL

# Verificar si ya existe un remote origin
if git remote | grep origin &> /dev/null; then
    echo "🔄 Remote origin ya existe. Actualizándolo..."
    git remote remove origin
fi

git remote add origin "$GITHUB_URL"
echo "✅ Repositorio remoto configurado."

# Crear .gitignore si no existe
if [ ! -f ".gitignore" ]; then
    echo "📝 Creando .gitignore básico..."
    cat <<EOL > .gitignore
# Archivos comunes a ignorar
/node_modules/
/build/
/dist/
/.idea/
/.vscode/
/.gradle/
/local.properties
*.apk
*.aab
*.zip
*.log
*.tmp
.DS_Store
Thumbs.db
EOL
fi

# Buscar archivos mayores a 100MB y agregarlos a .gitignore
echo "🔍 Buscando archivos pesados (+100MB)..."
find . -type f -size +100M > big_files.txt

if [ -s big_files.txt ]; then
    echo "⚠️ Archivos grandes detectados, agregándolos a .gitignore..."
    while IFS= read -r file; do
        echo "$file" >> .gitignore
        git rm --cached "$file" 2>/dev/null
    done < big_files.txt
    rm big_files.txt
else
    echo "✅ No se encontraron archivos pesados."
fi

# Añadir todos los archivos
git add .

# Commit inicial o actualización
read -p "📝 Escribe un mensaje para tu commit: " COMMIT_MSG
if [ -z "$COMMIT_MSG" ]; then
    COMMIT_MSG="Subida inicial del proyecto"
fi
git commit -m "$COMMIT_MSG"

# Subir al repositorio
echo "📤 Subiendo proyecto a GitHub..."
git branch -M main
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Proyecto subido correctamente a GitHub 🎉"
    echo "📌 URL: $GITHUB_URL"
else
    echo "❌ Error al subir el proyecto. Revisa tu conexión o tus credenciales."
fi
