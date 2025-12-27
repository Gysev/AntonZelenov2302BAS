#!/bin/bash
# Исправленный скрипт - использует конфигурационные файлы вместо -subj

cd "$(dirname "$0")" || exit

rm -rf certificates
mkdir -p certificates

STUDENT_ID="23265"

echo "[1/3] Generating Root CA..."

# Создаем конфиг для Root CA
cat > certificates/root-ca.conf <<EOF
[req]
distinguished_name = req_distinguished_name
prompt = no

[req_distinguished_name]
C = RU
ST = Moscow
L = Moscow
O = MTUCI
OU = IT
CN = Root-CA-${STUDENT_ID}
emailAddress = root@mtuci.ru
EOF

openssl genrsa -out certificates/root-ca-key.pem 4096
openssl req -new -x509 -days 3650 -key certificates/root-ca-key.pem -out certificates/root-ca-cert.pem -config certificates/root-ca.conf

echo "[2/3] Generating Intermediate CA..."

# Создаем конфиг для Intermediate CA
cat > certificates/intermediate-ca.conf <<EOF
[req]
distinguished_name = req_distinguished_name
prompt = no

[req_distinguished_name]
C = RU
ST = Moscow
L = Moscow
O = MTUCI
OU = IT
CN = Intermediate-CA-${STUDENT_ID}
emailAddress = intermediate@mtuci.ru
EOF

cat > certificates/intermediate-ca-ext.cnf <<EOF
[ v3_ca ]
basicConstraints = CA:TRUE
keyUsage = keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
EOF

openssl genrsa -out certificates/intermediate-ca-key.pem 4096
openssl req -new -key certificates/intermediate-ca-key.pem -out certificates/intermediate-ca.csr -config certificates/intermediate-ca.conf
openssl x509 -req -days 1825 -in certificates/intermediate-ca.csr -CA certificates/root-ca-cert.pem -CAkey certificates/root-ca-key.pem -CAcreateserial -out certificates/intermediate-ca-cert.pem -extensions v3_ca -extfile certificates/intermediate-ca-ext.cnf

echo "[3/3] Generating Server Certificate..."

# Создаем конфиг для Server Certificate
cat > certificates/server.conf <<EOF
[req]
distinguished_name = req_distinguished_name
prompt = no

[req_distinguished_name]
C = RU
ST = Moscow
L = Moscow
O = MTUCI
OU = IT
CN = localhost-${STUDENT_ID}
emailAddress = server@mtuci.ru
EOF

cat > certificates/server-ext.cnf <<EOF
[ v3_req ]
authorityKeyIdentifier = keyid,issuer
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = *.localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

openssl genrsa -out certificates/server-key.pem 4096
openssl req -new -key certificates/server-key.pem -out certificates/server.csr -config certificates/server.conf
openssl x509 -req -days 365 -in certificates/server.csr -CA certificates/intermediate-ca-cert.pem -CAkey certificates/intermediate-ca-key.pem -CAcreateserial -out certificates/server-cert.pem -extensions v3_req -extfile certificates/server-ext.cnf

echo "[4/4] Creating keystore..."
cat certificates/server-cert.pem certificates/intermediate-ca-cert.pem certificates/root-ca-cert.pem > certificates/chain.pem
openssl pkcs12 -export -in certificates/server-cert.pem -inkey certificates/server-key.pem -certfile certificates/chain.pem -out certificates/keystore.p12 -name "rbpo2025" -passout pass:changeit

echo ""
echo "=== Done! ==="
echo "Keystore: certificates/keystore.p12"
echo "Password: changeit"
echo ""
echo "Now copy keystore:"
echo "  cp certificates/keystore.p12 src/main/resources/"

