// lib/features/selection/data/datasources/selection_local_datasource.dart

import 'package:biblelib/core/errors/exceptions.dart';
import 'package:biblelib/core/network/local/daos/bible_dao.dart';
import 'package:biblelib/features/selection/data/models/bible_model.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../../core/constants/app_constants.dart';

abstract class SelectionLocalDataSource {
  Future<List<BibleModel>> getCachedBibles();
  Future<List<BibleModel>> getDownloadedBibles();
  Future<void> cacheBibles(List<BibleModel> bibles);
  Future<void> markBibleAsDownloaded(String bibleId);
  Future<bool> isFirstLaunch();
  Future<void> setFirstLaunchDone();
  Future<void> saveSelectedBibleIds(List<String> ids);
  Future<List<String>> getSelectedBibleIds();
  Future<void> setActiveBibleId(String id);
  Future<String?> getActiveBibleId();
}

class SelectionLocalDataSourceImpl implements SelectionLocalDataSource {
  final BibleDao _bibleDao;
  final SharedPreferences _prefs;

  const SelectionLocalDataSourceImpl(this._bibleDao, this._prefs);

  @override
  Future<List<BibleModel>> getCachedBibles() async {
    try {
      return await _bibleDao.getAllBibles();
    } catch (e) {
      throw CacheException('Failed to load cached bibles: $e');
    }
  }

  @override
  Future<List<BibleModel>> getDownloadedBibles() async {
    try {
      return await _bibleDao.getDownloadedBibles();
    } catch (e) {
      throw CacheException('Failed to load downloaded bibles: $e');
    }
  }

  @override
  Future<void> cacheBibles(List<BibleModel> bibles) async {
    try {
      await _bibleDao.insertBibles(bibles);
    } catch (e) {
      throw CacheException('Failed to cache bibles: $e');
    }
  }

  @override
  Future<void> markBibleAsDownloaded(String bibleId) async {
    try {
      await _bibleDao.markAsDownloaded(bibleId);
    } catch (e) {
      throw CacheException('Failed to mark bible as downloaded: $e');
    }
  }

  @override
  Future<bool> isFirstLaunch() async {
    return _prefs.getBool(kIsFirstLaunchKey) ?? true;
  }

  @override
  Future<void> setFirstLaunchDone() async {
    await _prefs.setBool(kIsFirstLaunchKey, false);
  }

  @override
  Future<void> saveSelectedBibleIds(List<String> ids) async {
    await _prefs.setStringList(kSelectedBibleIdsKey, ids);
  }

  @override
  Future<List<String>> getSelectedBibleIds() async {
    return _prefs.getStringList(kSelectedBibleIdsKey) ?? [];
  }

  @override
  Future<void> setActiveBibleId(String id) async {
    await _prefs.setString(kActiveBibleIdKey, id);
  }

  @override
  Future<String?> getActiveBibleId() async {
    return _prefs.getString(kActiveBibleIdKey);
  }
}
