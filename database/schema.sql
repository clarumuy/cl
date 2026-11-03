-- Base de datos del proyecto. Se aplica al crear el volumen por primera vez.
CREATE TABLE laboratories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    room VARCHAR(80) NOT NULL
);

CREATE TABLE computers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    ip VARCHAR(45) NOT NULL UNIQUE,
    laboratory_id BIGINT NOT NULL,
    CONSTRAINT fk_computer_lab FOREIGN KEY(laboratory_id) REFERENCES laboratories(id) ON DELETE RESTRICT
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE agent_nodes (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    computer_id BIGINT NULL UNIQUE,
    last_seen DATETIME(3) NULL,
    token_hash CHAR(64) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_agent_computer FOREIGN KEY(computer_id) REFERENCES computers(id) ON DELETE SET NULL
);

CREATE TABLE agent_commands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id VARCHAR(36) NOT NULL,
    action VARCHAR(30) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'pending',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    execute_at DATETIME(3) NOT NULL,
    expires_at DATETIME(3) NOT NULL,
    accepted_at DATETIME(3) NULL,
    INDEX idx_agent_pending (agent_id,status,execute_at),
    CONSTRAINT fk_command_agent FOREIGN KEY(agent_id) REFERENCES agent_nodes(id) ON DELETE CASCADE
);
