import 'package:froom/froom.dart';

import '../../../domain/models/bible_model.dart';

@Entity(tableName: 'bibles')
class BibleEntity {
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
  final int isDownloaded;
  final int isSelected;

  const BibleEntity({
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

  factory BibleEntity.fromJson(Map<String, dynamic> json) {
    final lang = json['language'] as Map<String, dynamic>? ?? {};
    return BibleEntity(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      nameLocal: json['nameLocal'] as String? ?? '',
      abbreviation: json['abbreviation'] as String? ?? '',
      abbreviationLocal: json['abbreviationLocal'] as String? ?? '',
      description: json['description'] as String? ?? '',
      language: lang['id'] as String? ?? '',
      languageLocal: lang['nameLocal'] as String? ?? '',
      languageScript: lang['script'] as String? ?? '',
      languageScriptDirection: lang['scriptDirection'] as String? ?? 'LTR',
      type: json['type'] as String? ?? '',
      updatedAt: json['updatedAt'] as String? ?? '',
    );
  }

  factory BibleEntity.fromModel(BibleModel model) => BibleEntity(
    id: model.id,
    name: model.name,
    nameLocal: model.nameLocal,
    abbreviation: model.abbreviation,
    abbreviationLocal: model.abbreviationLocal,
    description: model.description,
    language: model.language,
    languageLocal: model.languageLocal,
    languageScript: model.languageScript,
    languageScriptDirection: model.languageScriptDirection,
    type: model.type,
    updatedAt: model.updatedAt,
    isDownloaded: model.isDownloaded ? 1 : 0,
    isSelected: model.isSelected ? 1 : 0,
  );

  BibleModel toModel() => BibleModel(
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
