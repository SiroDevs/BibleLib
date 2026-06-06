// lib/features/selection/data/models/bible_model.dart

import 'package:froom/froom.dart';

import '../../domain/entities/bible_entity.dart';

@Entity(tableName: 'bibles')
class BibleModel {
  @PrimaryKey()
  final String id;
  final String name;
  final String nameLocal;
  final String abbreviation;
  final String abbreviationLocal;
  final String description;
  final String language;
  final String languageLocal;
  final String languageScript;
  final String languageScriptDirection;
  final String type;
  final String updatedAt;
  final int isDownloaded; // 0 or 1 (SQLite bool)
  final int isSelected;   // 0 or 1 — set when user picks this Bible on selection screen

  const BibleModel({
    required this.id,
    required this.name,
    required this.nameLocal,
    required this.abbreviation,
    required this.abbreviationLocal,
    required this.description,
    required this.language,
    required this.languageLocal,
    required this.languageScript,
    required this.languageScriptDirection,
    required this.type,
    required this.updatedAt,
    this.isDownloaded = 0,
    this.isSelected = 0,
  });

  factory BibleModel.fromJson(Map<String, dynamic> json) {
    final lang = json['language'] as Map<String, dynamic>? ?? {};
    return BibleModel(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      nameLocal: json['nameLocal'] as String? ?? '',
      abbreviation: json['abbreviation'] as String? ?? '',
      abbreviationLocal: json['abbreviationLocal'] as String? ?? '',
      description: json['description'] as String? ?? '',
      language: lang['id'] as String? ?? '',
      languageLocal: lang['nameLocal'] as String? ?? '',
      languageScript: lang['script'] as String? ?? '',
      languageScriptDirection:
          lang['scriptDirection'] as String? ?? 'LTR',
      type: json['type'] as String? ?? '',
      updatedAt: json['updatedAt'] as String? ?? '',
    );
  }

  factory BibleModel.fromEntity(BibleEntity entity) => BibleModel(
        id: entity.id,
        name: entity.name,
        nameLocal: entity.nameLocal,
        abbreviation: entity.abbreviation,
        abbreviationLocal: entity.abbreviationLocal,
        description: entity.description,
        language: entity.language,
        languageLocal: entity.languageLocal,
        languageScript: entity.languageScript,
        languageScriptDirection: entity.languageScriptDirection,
        type: entity.type,
        updatedAt: entity.updatedAt,
        isDownloaded: entity.isDownloaded ? 1 : 0,
        isSelected: entity.isSelected ? 1 : 0,
      );

  BibleEntity toEntity() => BibleEntity(
        id: id,
        name: name,
        nameLocal: nameLocal,
        abbreviation: abbreviation,
        abbreviationLocal: abbreviationLocal,
        description: description,
        language: language,
        languageLocal: languageLocal,
        languageScript: languageScript,
        languageScriptDirection: languageScriptDirection,
        type: type,
        updatedAt: updatedAt,
        isDownloaded: isDownloaded == 1,
        isSelected: isSelected == 1,
      );
}
