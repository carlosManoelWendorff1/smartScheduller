-- V9__add_professional_id_tenant_id_unique_constraint.sql
-- Enables a composite foreign key from professional_availability_rule, so the
-- database itself guarantees a professional can only get a rule under its
-- own tenant - not just that the professional exists somewhere.
ALTER TABLE professional ADD CONSTRAINT uq_professional_id_tenant_id UNIQUE (id, tenant_id);