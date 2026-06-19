package com.talenttrade.backend.model.entity;

/**
 * Distinguishes whether a Skill row represents something the user
 * can TEACH (offer to others) or wants to LEARN (acquire from others).
 *
 * This reuses the existing Skill entity from Module 3 instead of
 * introducing two separate tables — a user's "teach skills" and
 * "learn skills" are just Skill rows filtered by this enum.
 */
public enum SkillType {
    TEACH,
    LEARN
}
