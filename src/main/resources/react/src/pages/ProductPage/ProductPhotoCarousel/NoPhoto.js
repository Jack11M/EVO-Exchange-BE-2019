import React from 'react';

const NoPhoto = ( { noPhoto = 'noPhoto' }) => {
	console.log(noPhoto);
	return(
			<div className={noPhoto}>
				<svg width="299" height="281" viewBox="0 0 299 281" fill="none" xmlns="http://www.w3.org/2000/svg" className='noPhotoImg'>
					<path d="M264.959 0.349976H33.787C15.153 0.349976 0 15.498 0 34.154V246.615C0 265.249 15.153 280.381 33.787 280.381H264.958C283.592 280.381 298.729 265.249 298.729 246.615V34.154C298.73 15.498 283.593 0.349976 264.959 0.349976ZM193.174 50.623C211.194 50.623 225.808 65.238 225.808 83.257C225.808 101.276 211.193 115.891 193.174 115.891C175.149 115.891 160.54 101.276 160.54 83.257C160.54 65.238 175.149 50.623 193.174 50.623ZM254.363 249.149H149.362H49.039C40.026 249.149 36.012 242.628 40.075 234.583L96.081 123.653C100.139 115.609 107.873 114.891 113.35 122.048L169.666 195.644C175.143 202.802 184.716 203.411 191.052 196.998L204.829 183.047C211.16 176.634 220.488 177.428 225.655 184.809L261.33 235.768C266.487 243.16 263.376 249.149 254.363 249.149Z" fill="#EEEEEE"/>
				</svg>
			</div>
	)}
export default NoPhoto;
