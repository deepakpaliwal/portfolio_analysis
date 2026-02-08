--liquibase formatted sql

--changeset portfolio:dml-003-seed-sample-users context:local,dev
--comment: Seed sample users for development and testing (password is 'Password123!')
INSERT INTO users (email, password_hash, first_name, last_name, role, email_verified, auth_provider, subscription_tier) VALUES
('admin@portfolio-analysis.com', '$2a$12$LJ3m4ys3uz0G0.UfYNOT3eLVOA8WUi1WnMXfR0PjXHSJGVm/RiTm2', 'System', 'Admin', 'ADMIN', TRUE, 'LOCAL', 'PREMIUM'),
('trader@example.com', '$2a$12$LJ3m4ys3uz0G0.UfYNOT3eLVOA8WUi1WnMXfR0PjXHSJGVm/RiTm2', 'Jane', 'Trader', 'TRADER', TRUE, 'LOCAL', 'PREMIUM'),
('viewer@example.com', '$2a$12$LJ3m4ys3uz0G0.UfYNOT3eLVOA8WUi1WnMXfR0PjXHSJGVm/RiTm2', 'John', 'Viewer', 'VIEWER', TRUE, 'LOCAL', 'FREE'),
('pro.user@example.com', '$2a$12$LJ3m4ys3uz0G0.UfYNOT3eLVOA8WUi1WnMXfR0PjXHSJGVm/RiTm2', 'Alice', 'ProUser', 'TRADER', TRUE, 'LOCAL', 'PRO');

--rollback DELETE FROM users WHERE email IN ('admin@portfolio-analysis.com','trader@example.com','viewer@example.com','pro.user@example.com');
