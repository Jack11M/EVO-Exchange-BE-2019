import React from "react";
import { Link } from 'react-router-dom';
import {
  clothes,
  shoes,
  toys,
  transportForChildren,
  furniture,
  kidsUpToYear,
  books,
  other
} from "../../../img/all_images_export/navItems";
import { getTranslatedText } from '../../local/localisation';

import "./navCategory.scss";

const NavCategory = () => {
  return (
    <ul className="navbarBurger-list">
      <div className="wrapper">
        <li className="navbarBurger-element navbarBurger-element-clothes">
          <Link to={'/'}>
            <img src={clothes} alt="clothes" />
            <span>{getTranslatedText('header.clothes')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-shoes">
          <Link to={'/'}>
            <img src={shoes} alt="shoes" />
            <span>{getTranslatedText('header.shoes')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-toys">
          <Link to={'/'}>
            <img src={toys} alt="toys" />
            <span>{getTranslatedText('header.toys')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-transportForChildren">
          <Link to={'/'}>
            <img src={transportForChildren} alt="transportForChildren" />
            <span>{getTranslatedText('header.vehicles')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-furniture">
          <Link to={'/'}>
            <img src={furniture} alt="furniture" />
            <span>{getTranslatedText('header.furniture')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-kidsUpToYear">
          <Link to={'/'}>
            <img src={kidsUpToYear} alt="kidsUpToYear" />
            <span>{getTranslatedText('header.babies')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-books">
          <Link to={'/'}>
            <img src={books} alt="books" />
            <span>{getTranslatedText('header.books')}</span>
          </Link>
        </li>

        <li className="navbarBurger-element navbarBurger-element-other">
          <Link to={'/'}>
            <img src={other} alt="other" />
            <span>{getTranslatedText('header.another')}</span>
          </Link>
        </li>
      </div>
    </ul>
  );
};

export default NavCategory;
