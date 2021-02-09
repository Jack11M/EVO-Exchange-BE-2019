import React from 'react';
import { useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import BtnGoodBusiness from '../btnGoodBusiness/BtnGoodBusiness.js';
import { getTranslatedText } from '../local/localisation';
import logoFooter from '../../img/Logo-footer.png';
import { route } from '../../routes/routeConstants';

import './Footer.scss';

const Footer = () => {
	const { lang } = useSelector( state => state.auth );
	const timeDate = new Date();
	const yearNow = timeDate.getFullYear();

	return (
		<footer className="HomePageFooter">
			<div className="wrapper">
				<ul className="footer-blocks">
					<li className="footer-list">
						<span className="footer-list__icon icon-phone"/>
						<a
							href="mailto:&#101;&#109;&#97;&#105;&#108;&#46;&#111;&#98;&#109;&#101;&#110;&#121;&#97;&#115;&#104;&#107;&#97;&#64;&#103;&#109;&#97;&#105;&#108;&#46;&#99;&#111;&#109;">
							&#101;&#109;&#97;&#105;&#108;&#46;&#111;&#98;&#109;&#101;&#110;&#121;&#97;&#115;&#104;&#107;&#97;&#64;&#103;&#109;&#97;&#105;&#108;&#46;&#99;&#111;&#109;
						</a>
						<div className="footer-list-tel">
							<a href="tel:&#43;&#51;&#56;&#48;&#57;&#51;&#49;&#50;&#51;&#52;&#53;&#54;&#55;">
								+3 80 (93) 123 45 67
							</a>
							<a href="tel:&#43;&#51;&#56;&#48;&#57;&#51;&#49;&#50;&#51;&#52;&#53;&#54;&#55;">
								+3 80 (93) 123 45 67
							</a>
						</div>
					</li>
					<li className="footer-list">
						<span className="footer-list__icon icon-question"/>
						<Link to={ route.home } className="footer-list_rules">{ getTranslatedText( 'footer.rules', lang ) }</Link>

						<Link to={ route.home  } className="footer-list_rules">{ getTranslatedText( 'footer.charity', lang ) }</Link>
						<Link to={ route.home  } className="footer-list_rules">{ getTranslatedText( 'footer.questions', lang ) }</Link>
					</li>
					<li className="footer-list">
						<span className="footer-list__icon icon-home"/>
						<Link to={ route.home  }>
							<img src={ logoFooter } alt="Logo"/>
						</Link>
						<BtnGoodBusiness
							text={ getTranslatedText( 'header.goodness', lang ) }
							href={ route.home  }
							whatClass={ 'btn-Help-Children' }
						/>
					</li>
				</ul>
			</div>

			<div className="copyright">
				<span>&copy; { getTranslatedText( 'footer.protect', lang ) } </span>
				<span id="dataYear">{ yearNow } / Обменяшка</span>
			</div>
		</footer>
	);
};

export default Footer;
