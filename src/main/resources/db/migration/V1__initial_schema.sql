-- ============================================
-- Flyway Initial Schema Setup
-- ============================================
-- This is the initial Flyway migration script
-- It sets up the baseline schema structure
-- Actual tables will be created in subsequent migrations
--
-- Migration Version: 1.0
-- Date: 2026-07-03
-- ============================================

-- Set SQL dialect to PostgreSQL
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS public;

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set search path
SET search_path = public, pg_catalog;

-- ============================================
-- Note: Database tables will be managed by:
-- 1. Flyway migrations (for schema versioning)
-- 2. Hibernatevalidation mode (schema-first approach)
--
-- DDL Auto is set to 'validate' - Hibernate will
-- validate the database schema against entity
-- mappings at startup but will NOT modify it.
-- ============================================
