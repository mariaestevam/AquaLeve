/* =====================================================================
   AquaLeve — interações da interface (JavaScript puro)
   Cada bloco só age se encontrar na página os elementos que usa.
   ===================================================================== */
(function () {
    'use strict';

    var $ = function (sel, raiz) { return (raiz || document).querySelector(sel); };
    var $$ = function (sel, raiz) { return Array.prototype.slice.call((raiz || document).querySelectorAll(sel)); };
    var MESES = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];

    function chaveDia(d) {
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
    }
    function lerStorage(chave, padrao) {
        try { var v = localStorage.getItem(chave); return v ? JSON.parse(v) : padrao; } catch (e) { return padrao; }
    }
    function gravarStorage(chave, valor) {
        try { localStorage.setItem(chave, JSON.stringify(valor)); } catch (e) { /* navegador sem armazenamento */ }
    }
    function icone(nome, classe) {
        return '<svg class="i ' + (classe || '') + '" aria-hidden="true"><use href="#i-' + nome + '"/></svg>';
    }

    /* ---------- 1. Menu do celular ---------- */
    var header = $('.site-header');
    var botaoMenu = $('[data-menu-toggle]');
    if (header && botaoMenu) {
        botaoMenu.addEventListener('click', function () {
            var aberto = header.classList.toggle('aberto');
            botaoMenu.setAttribute('aria-expanded', aberto ? 'true' : 'false');
            botaoMenu.setAttribute('aria-label', aberto ? 'Fechar menu' : 'Abrir menu');
            botaoMenu.querySelector('use').setAttribute('href', aberto ? '#i-x' : '#i-menu');
        });
    }

    /* ---------- 2. Mensagens (toasts) ---------- */
    function fecharToast(t) {
        if (!t || t.classList.contains('saindo')) return;
        t.classList.add('saindo');
        setTimeout(function () { t.remove(); }, 220);
    }
    window.aqToast = function (texto, tipo) {
        var area = $('.toasts');
        if (!area) { area = document.createElement('div'); area.className = 'toasts'; document.body.appendChild(area); }
        var t = document.createElement('div');
        t.className = 'toast ' + (tipo || 'sucesso');
        t.setAttribute('role', tipo === 'erro' ? 'alert' : 'status');
        t.innerHTML = icone(tipo === 'erro' ? 'circle-alert' : tipo === 'info' ? 'info' : 'circle-check') + '<span></span><button type="button" class="fechar" aria-label="Fechar mensagem">' + icone('x', 'i-sm') + '</button>';
        t.querySelector('span').textContent = texto;
        area.appendChild(t);
        prepararToast(t);
    };
    function prepararToast(t) {
        var b = t.querySelector('.fechar');
        if (b) b.addEventListener('click', function () { fecharToast(t); });
        setTimeout(function () { fecharToast(t); }, t.classList.contains('erro') ? 9000 : 5000);
    }
    $$('.toasts .toast').forEach(prepararToast);

    /* ---------- 3. Confirmação antes de enviar (cancelar, excluir...) ---------- */
    var dialogo = $('#dialogo-confirmar');
    $$('form[data-confirmar]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (form.dataset.confirmado === 'sim') return;
            e.preventDefault();
            if (!dialogo || typeof dialogo.showModal !== 'function') {
                if (window.confirm(form.dataset.confirmar)) { form.dataset.confirmado = 'sim'; form.submit(); }
                return;
            }
            $('h2', dialogo).textContent = form.dataset.confirmar;
            $('p', dialogo).textContent = form.dataset.confirmarTexto || 'Esta ação não pode ser desfeita.';
            $('[data-ok]', dialogo).textContent = form.dataset.confirmarOk || 'Confirmar';
            $('[data-voltar]', dialogo).textContent = form.dataset.confirmarVoltar || 'Voltar';
            dialogo._form = form;
            dialogo.showModal();
        });
    });
    if (dialogo) {
        $('[data-voltar]', dialogo).addEventListener('click', function () { dialogo.close(); });
        $('[data-ok]', dialogo).addEventListener('click', function () {
            var f = dialogo._form; dialogo.close();
            if (f) { f.dataset.confirmado = 'sim'; f.submit(); }
        });
    }

    /* ---------- 4. Favoritos (coração nos cartões) ---------- */
    var favoritos = lerStorage('aqualeve-favoritos', []);
    if (!Array.isArray(favoritos)) favoritos = [];
    favoritos = favoritos.map(String);
    $$('.btn-favorito[data-id]').forEach(function (b) {
        var id = b.dataset.id, nome = b.dataset.nome || 'esta prainha';
        function pintar() {
            var ativo = favoritos.indexOf(id) >= 0;
            b.classList.toggle('ativo', ativo);
            b.setAttribute('aria-pressed', ativo ? 'true' : 'false');
            b.setAttribute('aria-label', ativo ? 'Remover ' + nome + ' dos favoritos' : 'Favoritar ' + nome);
        }
        pintar();
        b.addEventListener('click', function (e) {
            e.preventDefault();
            var pos = favoritos.indexOf(id);
            if (pos >= 0) favoritos.splice(pos, 1); else favoritos.push(id);
            gravarStorage('aqualeve-favoritos', favoritos);
            pintar();
        });
    });

    /* ---------- 5. Filtros da vitrine de prainhas ---------- */
    $$('[data-vitrine]').forEach(function (vitrine) {
        var grade = $('[data-grade]', vitrine);
        if (!grade) return;
        var cartoes = $$('.beach-card', grade);
        var filtroAtual = 'todas';
        var contador = $('[data-contador]', vitrine);
        var vazio = $('[data-vazio]', vitrine);
        var ordenar = $('[data-ordenar]', vitrine);

        function aplicar() {
            var visiveis = 0;
            cartoes.forEach(function (c) {
                var servicos = (c.dataset.servicos || '').toLowerCase();
                var favorito = favoritos.indexOf(c.dataset.id) >= 0;
                var ok = filtroAtual === 'todas' ||
                    (filtroAtual === 'favoritas' ? favorito : servicos.indexOf(filtroAtual) >= 0);
                c.hidden = !ok;
                if (ok) visiveis++;
            });
            if (contador) contador.textContent = visiveis + (visiveis === 1 ? ' prainha para explorar' : ' prainhas para explorar');
            if (vazio) vazio.hidden = visiveis > 0;
        }
        function ordenarCartoes() {
            if (!ordenar) return;
            var campo = ordenar.value;
            cartoes.slice().sort(function (a, b) {
                if (campo === 'nota') return (parseFloat(b.dataset.nota) || 0) - (parseFloat(a.dataset.nota) || 0);
                return (a.dataset[campo] || '').localeCompare(b.dataset[campo] || '', 'pt-BR');
            }).forEach(function (c) { grade.appendChild(c); });
        }
        $$('[data-filtro]', vitrine).forEach(function (b) {
            b.addEventListener('click', function () {
                filtroAtual = b.dataset.filtro;
                $$('[data-filtro]', vitrine).forEach(function (x) {
                    var ativo = x === b;
                    x.classList.toggle('btn-outline', !ativo);
                    x.setAttribute('aria-pressed', ativo ? 'true' : 'false');
                });
                aplicar();
            });
        });
        if (ordenar) ordenar.addEventListener('change', ordenarCartoes);
        $$('[data-limpar-filtros]', vitrine).forEach(function (b) {
            b.addEventListener('click', function () {
                var todas = $('[data-filtro="todas"]', vitrine);
                if (todas) todas.click();
                if (ordenar) { ordenar.value = 'nome'; ordenarCartoes(); }
            });
        });
        $$('.btn-favorito', vitrine).forEach(function (b) {
            b.addEventListener('click', function () { if (filtroAtual === 'favoritas') aplicar(); });
        });
        aplicar();
    });

    /* ---------- 6. Mostrar/ocultar bloco (ex.: "Filtros") ---------- */
    $$('[data-alternar]').forEach(function (b) {
        b.addEventListener('click', function () {
            var alvo = document.getElementById(b.dataset.alternar);
            if (!alvo) return;
            alvo.hidden = !alvo.hidden;
            b.setAttribute('aria-expanded', alvo.hidden ? 'false' : 'true');
        });
    });

    /* ---------- 7. Calendário de eventos ---------- */
    $$('[data-calendario]').forEach(function (cal) {
        var porDia = {}, liberados = {};
        $$('[data-cal-item]', cal).forEach(function (li) {
            var d = li.dataset.data;
            if (!d) return;
            if (li.dataset.tipo === 'liberado') { liberados[d] = true; return; }
            (porDia[d] = porDia[d] || []).push({ titulo: li.dataset.titulo, sub: li.dataset.sub });
        });
        var hoje = new Date();
        var cursor = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
        // se não há nada no mês atual, abre no mês do primeiro evento futuro
        var primeiros = Object.keys(porDia).concat(Object.keys(liberados)).filter(function (k) { return k >= chaveDia(hoje); }).sort();
        if (primeiros.length) {
            var p = primeiros[0].split('-');
            var mesAtual = chaveDia(cursor).slice(0, 7);
            if (!primeiros.some(function (k) { return k.slice(0, 7) === mesAtual; })) cursor = new Date(+p[0], +p[1] - 1, 1);
        }
        var selecionado = '';
        var titulo = $('[data-cal-titulo]', cal);
        var dias = $('[data-cal-dias]', cal);
        var selecao = $('[data-cal-selecao]', cal);
        var linkData = cal.dataset.linkData; // ex.: /prainhas/3?data=

        function desenhar() {
            var ano = cursor.getFullYear(), mes = cursor.getMonth();
            titulo.textContent = MESES[mes] + ' de ' + ano;
            var primeiroDia = new Date(ano, mes, 1).getDay();
            var total = new Date(ano, mes + 1, 0).getDate();
            var html = '';
            for (var v = 0; v < primeiroDia; v++) html += '<span></span>';
            for (var d = 1; d <= total; d++) {
                var data = new Date(ano, mes, d), k = chaveDia(data);
                var cls = 'cal-dia';
                var tem = !!porDia[k];
                if (tem) cls += ' evento';
                if (liberados[k]) cls += ' liberado';
                if (k === chaveDia(hoje)) cls += ' hoje';
                if (k === selecionado) cls += ' selecionado';
                var rotulo = 'Dia ' + d + (tem ? ' — dia com evento' : '') + (liberados[k] ? ' — liberado para reserva' : '');
                html += '<button type="button" class="' + cls + '" data-dia="' + k + '"' + (tem || (liberados[k] && linkData) ? '' : ' disabled') + ' aria-label="' + rotulo + '">' + d + '</button>';
            }
            dias.innerHTML = html;
            mostrarSelecao();
        }
        function mostrarSelecao() {
            if (!selecao) return;
            var itens = porDia[selecionado] || [];
            if (!selecionado || (!itens.length && !liberados[selecionado])) { selecao.hidden = true; selecao.innerHTML = ''; return; }
            var html = '';
            itens.forEach(function (ev) {
                html += '<div><p></p><small></small></div>';
            });
            if (liberados[selecionado] && linkData) {
                html += '<a class="btn btn-sm" href="' + linkData + selecionado + '#reservar">' + icone('calendar-check') + 'Consultar este dia</a>';
            }
            selecao.innerHTML = html;
            $$('div', selecao).forEach(function (div, i) {
                div.querySelector('p').textContent = itens[i].titulo || 'Evento';
                div.querySelector('small').textContent = itens[i].sub || '';
            });
            selecao.hidden = false;
        }
        dias.addEventListener('click', function (e) {
            var b = e.target.closest('[data-dia]');
            if (!b || b.disabled) return;
            selecionado = selecionado === b.dataset.dia ? '' : b.dataset.dia;
            desenhar();
        });
        $('[data-cal-anterior]', cal).addEventListener('click', function () { cursor = new Date(cursor.getFullYear(), cursor.getMonth() - 1, 1); selecionado = ''; desenhar(); });
        $('[data-cal-proximo]', cal).addEventListener('click', function () { cursor = new Date(cursor.getFullYear(), cursor.getMonth() + 1, 1); selecionado = ''; desenhar(); });
        desenhar();
    });

    /* ---------- 8. Estrelas da avaliação ---------- */
    $$('[data-estrelas]').forEach(function (grupo) {
        var campo = document.getElementById(grupo.dataset.estrelas);
        var botoes = $$('button', grupo);
        function pintar(n) { botoes.forEach(function (b, i) { b.classList.toggle('on', i < n); b.setAttribute('aria-pressed', i < n ? 'true' : 'false'); }); }
        botoes.forEach(function (b, i) {
            b.addEventListener('click', function () { campo.value = i + 1; pintar(i + 1); });
        });
        pintar(parseInt(campo.value, 10) || 0);
    });

    /* ---------- 9. Data + horário -> campo dataHora ---------- */
    $$('[data-data-hora]').forEach(function (form) {
        var destino = $('[name="dataHora"]', form);
        var dia = $('[data-dia-reserva]', form), hora = $('[data-hora-reserva]', form);
        function juntar() { if (dia && hora && dia.value && hora.value) destino.value = dia.value + 'T' + hora.value; }
        if (dia) dia.addEventListener('change', juntar);
        if (hora) hora.addEventListener('change', juntar);
        form.addEventListener('submit', juntar);
        juntar();
    });

    /* ---------- 10. Abas de "Minhas reservas" ---------- */
    $$('[data-abas]').forEach(function (grupo) {
        var lista = document.getElementById(grupo.dataset.abas);
        if (!lista) return;
        var itens = $$('[data-status]', lista);
        var vazio = $('[data-vazio-aba]');
        $$('[data-aba]', grupo).forEach(function (b) {
            b.addEventListener('click', function () {
                var aba = b.dataset.aba, visiveis = 0;
                $$('[data-aba]', grupo).forEach(function (x) { x.classList.toggle('btn-outline', x !== b); x.setAttribute('aria-pressed', x === b ? 'true' : 'false'); });
                itens.forEach(function (it) {
                    var s = it.dataset.status;
                    var ok = aba === 'todas' || (aba === 'ativas' ? (s === 'CONFIRMADA' || s === 'EM_USO') : (s === 'CANCELADA' || s === 'FINALIZADA'));
                    it.hidden = !ok;
                    if (ok) visiveis++;
                });
                if (vazio) vazio.hidden = visiveis > 0;
            });
        });
    });

    /* ---------- 11. Filtros de tabela + resumo + exportar CSV ---------- */
    $$('[data-tabela-filtrada]').forEach(function (bloco) {
        var tabela = $('table', bloco);
        if (!tabela) return;
        var linhas = $$('tbody tr', tabela);
        var busca = $('[data-filtro-busca]', bloco), status = $('[data-filtro-status]', bloco);
        var de = $('[data-filtro-de]', bloco), ate = $('[data-filtro-ate]', bloco);
        var vazio = $('[data-vazio-tabela]', bloco);
        function visiveis() { return linhas.filter(function (l) { return !l.hidden; }); }
        function aplicar() {
            var q = busca ? busca.value.trim().toLowerCase() : '';
            linhas.forEach(function (l) {
                var ok = (!q || (l.dataset.texto || l.textContent).toLowerCase().indexOf(q) >= 0) &&
                    (!status || !status.value || l.dataset.status === status.value) &&
                    (!de || !de.value || (l.dataset.data || '') >= de.value) &&
                    (!ate || !ate.value || (l.dataset.data || '') <= ate.value);
                l.hidden = !ok;
            });
            var v = visiveis();
            if (vazio) vazio.hidden = v.length > 0;
            var r = $('[data-resumo]', bloco);
            if (r) {
                var ativas = v.filter(function (l) { return l.dataset.status !== 'CANCELADA'; });
                var pessoas = ativas.reduce(function (s, l) { return s + (parseInt(l.dataset.pessoas, 10) || 0); }, 0);
                $('[data-resumo-total]', r).textContent = v.length + (v.length === 1 ? ' reserva' : ' reservas');
                $('[data-resumo-pessoas]', r).textContent = pessoas + ' visitantes previstos';
                $('[data-resumo-ativas]', r).textContent = ativas.length + ' ativas ou concluídas';
            }
        }
        [busca, status, de, ate].forEach(function (c) { if (c) c.addEventListener('input', aplicar); });
        $$('[data-limpar-tabela]', bloco).forEach(function (b) {
            b.addEventListener('click', function () { [busca, status, de, ate].forEach(function (c) { if (c) c.value = ''; }); aplicar(); });
        });
        var exportar = $('[data-exportar-csv]', bloco);
        if (exportar) {
            exportar.addEventListener('click', function () {
                var cab = $$('thead th', tabela).filter(function (th) { return !th.hasAttribute('data-sem-csv'); });
                var idx = cab.map(function (th) { return th.cellIndex; });
                var dados = [cab.map(function (th) { return th.textContent.trim(); })];
                visiveis().forEach(function (l) {
                    dados.push(idx.map(function (i) { return (l.cells[i] ? l.cells[i].innerText : '').replace(/\s+/g, ' ').trim(); }));
                });
                var esc = function (s) { return '"' + ((/^[=+@\-\t\r]/.test(s) ? "'" : '') + s.replace(/"/g, '""')) + '"'; };
                var csv = '﻿' + dados.map(function (l) { return l.map(esc).join(';'); }).join('\r\n');
                var url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
                var a = document.createElement('a');
                a.href = url; a.download = exportar.dataset.arquivo || 'aqualeve-relatorio.csv';
                document.body.appendChild(a); a.click(); a.remove();
                setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
                window.aqToast('Relatório exportado.', 'sucesso');
            });
        }
        aplicar();
    });
})();
