import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../domain/entities/bible_entity.dart';

class BibleListItem extends StatelessWidget {
  final BibleEntity bible;
  final bool isSelected;
  final VoidCallback onTap;

  const BibleListItem({
    super.key,
    required this.bible,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 5),
      decoration: BoxDecoration(
        color: isSelected
            ? AppColors.primary.withValues(alpha: 0.1)
            : theme.cardTheme.color,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isSelected ? AppColors.primary : Colors.transparent,
          width: 2,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: ListTile(
        onTap: onTap,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: _LanguageBadge(
          script: bible.languageScriptDirection,
          abbreviation: bible.abbreviation,
        ),
        title: Text(
          bible.name,
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w600,
            color: isSelected ? AppColors.primary : null,
          ),
        ),
        subtitle: Text(
          '${bible.language.toUpperCase()} · ${bible.abbreviation}',
          style: theme.textTheme.bodyMedium?.copyWith(
            color: theme.textTheme.bodyMedium?.color?.withValues(alpha: 0.6),
          ),
        ),
        trailing: isSelected
            ? const Icon(Icons.check_circle, color: AppColors.primary)
            : const Icon(Icons.circle_outlined, color: Colors.grey),
      ),
    );
  }
}

class _LanguageBadge extends StatelessWidget {
  final String script;
  final String abbreviation;

  const _LanguageBadge({required this.script, required this.abbreviation});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 48,
      height: 48,
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(8),
      ),
      alignment: Alignment.center,
      child: Text(
        abbreviation.length > 3
            ? abbreviation.substring(0, 3)
            : abbreviation,
        style: const TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.bold,
          color: AppColors.primary,
        ),
      ),
    );
  }
}
