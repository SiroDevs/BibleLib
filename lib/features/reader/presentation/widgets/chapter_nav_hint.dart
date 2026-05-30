// lib/features/reader/presentation/widgets/chapter_nav_hint.dart

import 'package:biblelib/core/theme/app_colors.dart';
import 'package:flutter/material.dart';

class ChapterNavHint extends StatefulWidget {
  final bool hasNext;
  final bool hasPrevious;

  const ChapterNavHint({
    super.key,
    required this.hasNext,
    required this.hasPrevious,
  });

  @override
  State<ChapterNavHint> createState() => _ChapterNavHintState();
}

class _ChapterNavHintState extends State<ChapterNavHint>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _fade;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );
    _fade = CurvedAnimation(parent: _controller, curve: Curves.easeIn);
    _controller.forward();
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) _controller.reverse();
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _fade,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 12),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (widget.hasPrevious)
              const _HintChip(
                icon: Icons.arrow_back_ios_new_rounded,
                label: 'Previous',
              ),
            if (widget.hasPrevious && widget.hasNext)
              const SizedBox(width: 16),
            if (widget.hasNext)
              const _HintChip(
                icon: Icons.arrow_forward_ios_rounded,
                label: 'Next',
                trailingIcon: true,
              ),
          ],
        ),
      ),
    );
  }
}

class _HintChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool trailingIcon;

  const _HintChip({
    required this.icon,
    required this.label,
    this.trailingIcon = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.primary.withOpacity(0.1),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (!trailingIcon) ...[
            Icon(icon, size: 12, color: AppColors.primary),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              color: AppColors.primary,
              fontWeight: FontWeight.w500,
            ),
          ),
          if (trailingIcon) ...[
            const SizedBox(width: 4),
            Icon(icon, size: 12, color: AppColors.primary),
          ],
        ],
      ),
    );
  }
}
