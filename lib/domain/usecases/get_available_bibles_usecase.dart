// lib/features/selection/domain/usecases/get_available_bibles_usecase.dart

import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/bible_entity.dart';
import '../repositories/selection_repository.dart';

class GetAvailableBiblesUseCase {
  final SelectionRepository _repository;
  const GetAvailableBiblesUseCase(this._repository);

  Future<Either<Failure, List<BibleEntity>>> call() =>
      _repository.getAvailableBibles();
}
