import { call, put } from 'redux-saga/effects';

import { putUserChildren } from '../../../../REST/Resources';
import { startFetching, stopFetching } from '../../../ui/action';
import { unauthorized } from '../../../auth/action';
import { receivedPutRequestChildrenSync } from '../../profileAction';

export function* workerUserChildrenPut( action ) {
	const body = action.payload;
	yield put( startFetching() );
	try {
		const { data } = yield call( putUserChildren, body );
		yield put( receivedPutRequestChildrenSync( data ) );
	} catch (e) {
		console.log( e.response.data );
		if (e.response.status === 401) {
			yield put( unauthorized() );
		}
	} finally {
		yield put( stopFetching() );
	}
}
