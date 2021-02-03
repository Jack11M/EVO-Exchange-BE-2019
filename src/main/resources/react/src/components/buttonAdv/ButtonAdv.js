import React from "react";
import { useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { getTranslatedText } from '../local/localisation';

import "./buttonAdv.scss";

const ButtonAdv = () => {
  const { lang } =useSelector(state => state.auth)
  return (
    <Link to={'/'} className="btn-adv">
      <span>{ getTranslatedText('button.addAdv', lang) }</span>
    </Link>
  );
};

export default ButtonAdv;
