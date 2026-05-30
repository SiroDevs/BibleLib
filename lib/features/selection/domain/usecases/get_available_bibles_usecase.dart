// lib/features/selection/domain/usecases/get_available_bibles_usecase.dart

import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/selection/domain/entities/bible_entity.dart';
import 'package:biblelib/features/selection/domain/repositories/selection_repository.dart';
import 'package:dartz/dartz.dart';

class GetAvailableBiblesUseCase {
  final SelectionRepository _repository;
  const GetAvailableBiblesUseCase(this._repository);

  Future<Either<Failure, List<BibleEntity>>> call() =>
      _repository.getAvailableBibles();
}
