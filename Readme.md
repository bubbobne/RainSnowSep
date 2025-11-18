 



## Model detail

To separate rain and snow this model implemets the method reported in Formetta et al. (2014)

$$
\begin{cases}
P_r(t) = \alpha_r \left( \dfrac{P(t)}{\pi}\,\arctan\!\left( \dfrac{T - T_s}{m_1} \right) + \dfrac{P}{2} \right) \\
\\
P_s(t) = \alpha_s \left( P(t) - P_r(t) \right)
\end{cases}
$$

| Symbol      | Meaning                                      | Unit                          |
|-------------|----------------------------------------------|-------------------------------|
| $$P(t) $$    | Precipitation at time *t*                    | [LT⁻¹]                        |
| $$P_r(t)$$    | Rainfall at time *t*                         | [LT⁻¹]                        |
| $$P_s(t)$$   | Snowfall at time *t*                         |  [LT⁻¹]                         |
| $$α_r$$      | Adjustment coefficient for rain measurements | –                             |
| $$α_s$$       | Adjustment coefficient for snow measurements | –                             |
| $$m_1$$    | Smoothing degree parameter                   | –                             |





Formetta, G., Kampf, S. K., David, O., & Rigon, R. (2014, 6 May). Snow water
equivalent modeling components in NewAge-JGrass. Geosci. Model Dev., 7 (3),
725–736.

